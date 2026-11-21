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

package com.neep.neepmeat.machine.power_flower;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PowerFlowerSeedsBlock extends BaseBlock
{
    private final VoxelShape shape = Block.box(0, 0, 0, 16, 5, 16);

    public PowerFlowerSeedsBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion());
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context)
    {
        return false;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return shape;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        return validBlock(world.getBlockState(pos.below()).getBlock()) && super.canSurvive(state, world, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);
    }

    private boolean validBlock(Block state)
    {
        return state.defaultBlockState().is(BlockTags.DIRT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return super.getStateForPlacement(ctx);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
        grow(world, pos, state, random);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
//        if (!world.isClient())
//        {
//            grow(world, pos, state, world.getRandom());
//        }
        return InteractionResult.SUCCESS;
    }

    public void grow(Level world, BlockPos pos, BlockState state, RandomSource random)
    {
        Direction[] horizontal = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        boolean controller = false;

        world.setBlockAndUpdate(pos, NMBlocks.POWER_FLOWER_GROWTH.defaultBlockState());

        BlockPos.MutableBlockPos mutable = pos.mutable();
        for (Direction d : horizontal)
        {
            mutable.setWithOffset(pos, d);
            for (int i = 0; i < 2; ++i)
            {
                BlockState nextState = world.getBlockState(mutable);
                if (nextState.isAir() || nextState.is(NMBlocks.POWER_FLOWER_SEEDS))
                    world.setBlockAndUpdate(mutable, NMBlocks.POWER_FLOWER_GROWTH.defaultBlockState());

                Direction direction = horizontal[random.nextInt(4)];
                mutable.setWithOffset(mutable, direction);
            }
        }

        int height = Math.max(4, random.nextInt(6));
        for (int j = 0; j < height; ++j)
        {
            BlockPos stem = mutable.setWithOffset(pos, 0, j, 0).immutable();
            BlockState nextState = world.getBlockState(stem);
            if (canGrowInto(nextState))
            {
                world.setBlockAndUpdate(stem, NMBlocks.POWER_FLOWER_GROWTH.getState(world, stem));
            }

            for (Direction d : horizontal)
            {
                mutable.setWithOffset(stem, d);
                if (random.nextFloat() > ((float) j / height))
                {
                    BlockState thing = world.getBlockState(mutable);
                    if (canGrowInto(thing) && !world.getBlockState(mutable.below()).isAir())
                    {
                        if (!controller && random.nextFloat() < 0.4)
                        {
                            world.setBlockAndUpdate(mutable, NMBlocks.POWER_FLOWER_CONTROLLER.defaultBlockState());
                            controller = true;
                        }
                        else
                        {
                            world.setBlockAndUpdate(mutable, NMBlocks.POWER_FLOWER_GROWTH.getState(world, mutable));
                        }
                    }
                }
            }

            // Make sure there will definitely be a controller
            if (j == height - 1 && !controller)
            {
                world.setBlockAndUpdate(stem, NMBlocks.POWER_FLOWER_CONTROLLER.defaultBlockState());
            }
        }
    }

    public static boolean canGrowInto(BlockState state)
    {
        return state.isAir() || state.is(NMBlocks.POWER_FLOWER_SEEDS);
    }
}
