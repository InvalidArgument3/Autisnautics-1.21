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

package com.neep.neepmeat.block.redstone;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.block.entity.BigLeverBlockEntity;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMSounds;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BigLeverBlock extends LeverBlock implements MeatlibBlock, EntityBlock
{
    static final VoxelShape NORTH_AABB = Block.box(4.0, 1.0, 10.0, 12.0, 15.0, 16.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(4.0, 1.0, 0.0, 12.0, 15.0, 6.0);
    protected static final VoxelShape WEST_AABB = Block.box(10.0, 1.0, 4.0, 16.0, 15.0, 12.0);
    protected static final VoxelShape EAST_AABB = Block.box(0.0, 1.0, 4.0, 6.0, 15.0, 12.0);
    protected static final VoxelShape UP_AABB_Z = Block.box(4.0, 0.0, 1.0, 12.0, 8.0, 15.0);
    protected static final VoxelShape UP_AABB_X = Block.box(1.0, 0.0, 4.0, 15.0, 8.0, 12.0);
    protected static final VoxelShape DOWN_AABB_Z = Block.box(4.0, 8.0, 1.0, 12.0, 16.0, 12.0);
    protected static final VoxelShape DOWN_AABB_X = Block.box(1.0, 8.0, 4.0, 15.0, 16.0, 12.0);

    protected BlockItem blockItem;

    public BigLeverBlock(RegistrationContext c, Properties settings)
    {
        super(settings);
        this.blockItem = new BaseBlockItem(this, c, ItemSettings.block().tooltip(TooltipSupplier.simple(1)));

    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type)
    {
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        switch (state.getValue(FACE))
        {
            case FLOOR:
            {
                switch (state.getValue(FACING).getAxis())
                {
                    case X:
                    {
                        return UP_AABB_X;
                    }
                }
                return UP_AABB_Z;
            }
            case WALL:
            {
                switch (state.getValue(FACING))
                {
                    case EAST:
                    {
                        return EAST_AABB;
                    }
                    case WEST:
                    {
                        return WEST_AABB;
                    }
                    case SOUTH:
                    {
                        return SOUTH_AABB;
                    }
                }
                return NORTH_AABB;
            }
        }
        switch (state.getValue(FACING).getAxis())
        {
            case X:
            {
                return DOWN_AABB_X;
            }
        }
        return DOWN_AABB_Z;
    }

    public BlockItem getBlockItem()
    {
        return blockItem;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new BigLeverBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown())
        {
            BlockState blockState = this.pull(state, world, pos);
            ((BigLeverBlockEntity) world.getBlockEntity(pos)).togglePower();

        }
        else
        {
            BigLeverBlockEntity be = ((BigLeverBlockEntity) world.getBlockEntity(pos));
            if (be.activeTicks > 0)
                be.activeTicks = 0;
            else
                be.activeTicks = 40;

            world.playSound(null, pos, MeatlibSounds.CLICK, SoundSource.BLOCKS, 0.7f, 2);

//            Direction facing = state.get(FACING);
//            Direction hitSide = hit.getSide();
//            if (hitSide == facing.rotateYClockwise() || hitSide == facing.rotateYCounterclockwise())
//            {
//                Vec3d hitPos = hit.getPos();
//                NMVec2f relative = NMMaths.removeAxis(hitSide.getAxis(), hitPos.subtract(pos.getX(), pos.getY(), pos.getZ()));
//                boolean increasing = relative.getX() > 0.25;
//                world.playSound(null, pos, SoundInitialiser.CLICK, SoundCategory.BLOCKS, 0.4f, increasing ? 2 : 1);
//            }
        }
        if (world.isClientSide)
            return InteractionResult.SUCCESS;
        return InteractionResult.CONSUME;
    }

    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(this))
        {
            world.removeBlockEntity(pos);
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A>
    checkType(BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<E> ticker, Level world)
    {
        return expectedType == givenType && !world.isClientSide ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return checkType(type, NMBlockEntities.BIG_LEVER, BigLeverBlockEntity::serverTick, world);
    }

    public void setPowered(Level world, BlockPos pos, boolean powered)
    {
        BlockState state = world.getBlockState(pos).setValue(POWERED, powered);
        world.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
        world.setBlock(pos, state, Block.UPDATE_ALL);

        world.updateNeighborsAt(pos, this);
        world.updateNeighborsAt(pos.relative(LeverBlock.getConnectedDirection(state).getOpposite()), this);

        if (powered)
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), NMSounds.BIG_LEVER_ON.get(), SoundSource.HOSTILE, 1f, 0.8f);
        else
            world.playSound(null, pos, NMSounds.BIG_LEVER_OFF.get(), SoundSource.HOSTILE, 1f, 0.9f);

    }
}
