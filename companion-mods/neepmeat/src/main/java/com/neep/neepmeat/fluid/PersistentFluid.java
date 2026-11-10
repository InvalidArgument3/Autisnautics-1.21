/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.fluid;

import com.neep.meatlib.fluid.BaseFluid;
import com.neep.neepmeat.init.NMParticles;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import java.util.List;

public abstract class PersistentFluid extends BaseFluid
{
    public static void incrementLevel(Level world, BlockPos pos, BlockState state, FlowingFluid fluid)
    {
        FluidState fluidstate = world.getFluidState(pos);
        if (!fluidstate.isEmpty() && !fluidstate.getType().equals(fluid))
            return;

        if (fluidstate.isEmpty() && state.is(Blocks.AIR))
        {
            world.setBlock(pos, fluid.getFlowing(1, false).createLegacyBlock(), Block.UPDATE_ALL);
            return;
        }

        int level = fluidstate.getAmount();
//        world.setBlockState(pos, fluid.getFlowing(8, false).getBlockState(), Block.NOTIFY_ALL);
        world.setBlock(pos, fluid.getFlowing(level + 1, false).createLegacyBlock(), Block.UPDATE_ALL);
    }

    @Override
    protected int getSlopeFindDistance(LevelReader world)
    {
        return 10;
    }

    @Override
    public int getTickDelay(LevelReader world)
    {
        return 10;
    }

    @Override
    protected float getExplosionResistance()
    {
        return 100;
    }

    @Override
    public void tick(Level world, BlockPos pos, FluidState oldState)
    {
        FluidState newState = updateFlow(world, pos, oldState);

        if (!oldState.isSource())
        {
            int i = this.getSpreadDelay(world, pos, oldState, newState);
            if (newState.isEmpty())
            {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            else if (!newState.equals(oldState))
            {
                BlockState blockState = newState.createLegacyBlock();
                world.setBlock(pos, blockState, Block.UPDATE_CLIENTS);
                world.scheduleTick(pos, newState.getType(), i);
                world.updateNeighborsAt(pos, blockState.getBlock());
            }
        }

    }

    @Override
    protected FluidState getNewLiquid(Level world, BlockPos pos, BlockState state)
    {
        return state.getFluidState();
    }

    protected FluidState updateFlow(Level world, BlockPos fluidPos, FluidState state)
    {
        if (state.isEmpty())
        {
            return state;
        }

        // Try to distribute current level across surrounding blocks.
        int level = state.getAmount();

        {
            BlockPos targetPos = fluidPos.relative(Direction.DOWN, 1);
            BlockState targetState = world.getBlockState(targetPos);
            int targetLevel = targetState.getFluidState().getAmount();
            if (canFill(world, targetPos, targetState))
            {
                if (targetLevel < 8 && level > 0)
                {
                    int toDown = Math.min(8 - targetLevel, level);

                    if (targetLevel + toDown > 0)
                        trySetBlockState(world, targetPos, this.getFlowing(targetLevel + toDown, false).createLegacyBlock(), targetState);

                    level -= toDown;
                }
            }
        }

        List<ObjectBooleanPair<BlockPos>> posList = new ObjectArrayList<>();
        var directions = Direction.Plane.HORIZONTAL.shuffledCopy(world.getRandom());

        if (level > 0)
        {
            for (Direction direction : directions)
            {
                BlockPos adj = fluidPos.relative(direction);
                BlockPos adjDown = adj.below();
                BlockState adjState = world.getBlockState(adj);
                BlockState adjDownState = world.getBlockState(adjDown);

                boolean canFillDown = canFill(world, adjDown, adjDownState);

                if (!canFillDown)
                {
                    // Check this fluid
                    int otherLevel = adjState.getFluidState().getAmount();
                    if (canFill(world, adj, adjState) && (otherLevel < level) && (level > 1))
                    {
                        posList.add(ObjectBooleanPair.of(adj, false));
                    }
                }
                else
                {
                    // Look down
                    int otherLevel = adjDownState.getFluidState().getAmount();
                    if (canFill(world, adjDown, adjDownState) && otherLevel < 8)
                    {
                        posList.add(ObjectBooleanPair.of(adjDown, true));
                    }
                }
            }

            // Prioritise neighbours with lower fluid levels.
            // Distribute fluid across list of positions
            for (var pos : posList)
            {
                // Break if there is too little fluid, but also allow flowing down
                boolean canEmpty = pos.secondBoolean();
                if (level <= (canEmpty ? 0 : 1))
                    break;

                BlockState targetState = world.getBlockState(pos.first());
                int targetLevel = targetState.getFluidState().getAmount();

                trySetBlockState(world, pos.first(), this.getFlowing(targetLevel + 1, false).createLegacyBlock(), targetState);
                --level;

            }
        }

        if (level != state.getAmount())
        {
            BlockState newState = level > 0 ? this.getFlowing(level, false).createLegacyBlock() : Blocks.AIR.defaultBlockState();
            world.setBlock(fluidPos, newState, Block.UPDATE_ALL);
            return newState.getFluidState();
        }

        return state;
    }

    private void trySetBlockState(Level world, BlockPos pos, BlockState state, BlockState prevState)
    {
        // Only replace air. No handling for dropping items.
        // Spawn particles when destroyed.

        if (prevState.isAir() || prevState.is(state.getBlock()))
            world.setBlock(pos, state, Block.UPDATE_ALL);
        else if (world instanceof ServerLevel serverWorld)
            serverWorld.sendParticles(new BlockParticleOption(NMParticles.FLUID_BITS, state),
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    10,
                    0.5, 0.5, 0.5, 0.1);
    }

//    private boolean canFill(BlockView world, BlockPos pos, BlockState state)
//    {
//        Block block = state.getBlock();
////        if (block instanceof FluidFillable)
////        {
////            return ((FluidFillable) block).canFillWithFluid(world, pos, state, fluid);
////        }
//        if (state.getFluidState().isOf(this))
//            return true;
//
//        if (!(block instanceof DoorBlock) && !state.isIn(BlockTags.SIGNS) && !state.isOf(Blocks.LADDER) && !state.isOf(Blocks.SUGAR_CANE) && !state.isOf(Blocks.BUBBLE_COLUMN))
//        {
//            if (!state.isOf(Blocks.NETHER_PORTAL) && !state.isOf(Blocks.END_PORTAL) && !state.isOf(Blocks.END_GATEWAY) && !state.isOf(Blocks.STRUCTURE_VOID))
//                return !state.blocksMovement();
//            else
//                return false;
//        }
//        else
//        {
//            return false;
//        }
//    }

    private boolean canFill(BlockGetter world, BlockPos pos, BlockState state)
    {
        // Vanilla mechanics
        Block block = state.getBlock();
        if (block instanceof LiquidBlockContainer)
        {
            return ((LiquidBlockContainer) block).canPlaceLiquid(world, pos, state, this);
        }
        else if (block instanceof DoorBlock
                || state.is(BlockTags.SIGNS)
                || state.is(Blocks.LADDER)
                || state.is(Blocks.SUGAR_CANE)
                || state.is(Blocks.BUBBLE_COLUMN))
        {
            return false;
        }
        else
        {
            return !state.is(Blocks.NETHER_PORTAL) && !state.is(Blocks.END_PORTAL) && !state.is(Blocks.END_GATEWAY) && !state.is(Blocks.STRUCTURE_VOID)
                    ? !state.blocksMotion()
                    : false;
        }
    }

    public long getAmount(FluidState fluidState)
    {
        return fluidState.getAmount() * FluidConstants.BLOCK / 8;
    }

    public FluidVariant getVariant(FluidState fluidState)
    {
        return FluidVariant.of(this);
    }

    public boolean extractOrNothing(Level world, BlockPos pos, FluidState fluidState, long amount)
    {
        if (amount % (FluidConstants.BLOCK / 8) != 0)
            return false;

        int levels = Math.toIntExact(amount / (FluidConstants.BLOCK / 8));

        int maxLevel = fluidState.getAmount();

        if (levels > maxLevel)
            return false;

        int newLevel = maxLevel - levels;
        BlockState newState;
        if (newLevel == 0)
            newState = Blocks.AIR.defaultBlockState();
        else
            newState = getFlowing(newLevel, false).createLegacyBlock();

        world.setBlockAndUpdate(pos, newState);
        return true;
    }
}
