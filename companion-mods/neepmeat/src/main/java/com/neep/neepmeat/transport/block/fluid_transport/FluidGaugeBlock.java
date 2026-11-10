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

package com.neep.neepmeat.transport.block.fluid_transport;

import com.google.common.collect.ImmutableMap;
import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.transport.block.fluid_transport.entity.FluidGaugeBlockEntity;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FluidGaugeBlock<T> extends BaseFacingBlock implements EntityBlock
{
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 7);

    protected final Map<Direction, VoxelShape> SHAPE_MAP = ImmutableMap.of(
            Direction.NORTH, Block.box(6, 0, 0, 10, 16, 2),
            Direction.EAST, Block.box(14, 0, 6, 16, 16, 10),
            Direction.SOUTH, Block.box(6, 0, 14, 10, 16, 16),
            Direction.WEST, Block.box(0, 0, 6, 2, 16, 10),
            Direction.UP, Block.box(6, 14, 0, 10, 16, 16),
            Direction.DOWN, Block.box(6, 0, 0, 10, 2, 16)
    );

    private final Supplier<BlockEntityType<FluidGaugeBlockEntity<T>>> typeSupplier;

    public FluidGaugeBlock(RegistrationContext ctx, Supplier<BlockEntityType<FluidGaugeBlockEntity<T>>> type, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
        this.typeSupplier = type;
    }

    public static BlockState getLevelState(BlockState old, int comparatorLevel)
    {
        int level = comparatorLevel > 0 && comparatorLevel <= 2 ? 1 : (int) Mth.clamp(comparatorLevel / 15f * 7, 0, 7);
        return old.setValue(LEVEL, level);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return SHAPE_MAP.get(state.getValue(FACING));
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
    {
        if (world.getBlockEntity(pos) instanceof FluidGaugeBlockEntity<?> be)
        {
            return be.getOutput();
        }

        return 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
    {
        if (direction == state.getValue(FACING) && world.getBlockEntity(pos) instanceof FluidGaugeBlockEntity<?> be)
        {
            return be.getOutput();
        }

        return 0;
    }

    public void updateNeighbors(Level world, BlockPos pos, BlockState state)
    {
        world.blockUpdated(pos, this);

        Direction direction = state.getValue(FluidGaugeBlock.FACING);
        BlockPos blockPos = pos.relative(direction.getOpposite());
        world.neighborChanged(blockPos, this, pos);
        world.updateNeighborsAtExceptFromFacing(blockPos, this, direction);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(state.getBlock()))
        {
            if (!world.isClientSide())
            {
                BlockState blockState = state.setValue(LEVEL, 0);
//                world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
                this.updateNeighbors(world, pos, blockState);
            }
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public boolean isSignalSource(BlockState state)
    {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LEVEL);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return typeSupplier.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, typeSupplier.get(), (world1, pos, state1, blockEntity) -> blockEntity.serverTick(), null, world);
    }
}
