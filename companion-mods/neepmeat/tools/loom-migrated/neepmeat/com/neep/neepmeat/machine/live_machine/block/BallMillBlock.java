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

package com.neep.neepmeat.machine.live_machine.block;

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block_2.BigBlock2;
import com.neep.neepmeat.api.big_block_2.BigBlock2Structure;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.block.entity.BallMillBlockEntity;
import com.neep.meatlib.util.MiscUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class BallMillBlock extends BigBlock2 implements EntityBlock
{
    public static final EnumProperty<Direction.Axis> AXIS = EnumProperty.create("axis", Direction.Axis.class, Direction.Axis.X, Direction.Axis.Z);
    public static final EnumProperty<AxisConnection> CONNECTION = EnumProperty.create("connection", AxisConnection.class);

    private final EnumMap<Direction.Axis, Int2ObjectMap<Vec3i>> offsetMap;
    private final EnumMap<Direction.Axis, BigBlockPattern> patternMap;

    public BallMillBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings, 8);

        offsetMap = new EnumMap<>(Direction.Axis.class);
        patternMap = new EnumMap<>(Direction.Axis.class);

        var x = BigBlockPattern.makeRange(0, -1, -1, 0, 1, 1, Blocks.AIR.defaultBlockState());
        var z = BigBlockPattern.makeRange(-1, -1, 0, 1, 1, 0, Blocks.AIR.defaultBlockState());

        patternMap.put(Direction.Axis.X, x);
        offsetMap.put(Direction.Axis.X, x.encode());
        patternMap.put(Direction.Axis.Z, z);
        offsetMap.put(Direction.Axis.Z, z.encode());

        registerDefaultState(getStateDefinition().any().setValue(CONNECTION, AxisConnection.NONE));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        if (!ctx.getClickedFace().getAxis().isVertical())
        {
            Direction.Axis axis = ctx.getClickedFace().getAxis();
            BlockState state = this.defaultBlockState().setValue(AXIS, axis);
            BlockPos pos = ctx.getClickedPos();

            state = state.setValue(CONNECTION, getConnection(ctx.getLevel(), pos, state));

            return state;
        }

        return null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        AxisConnection newConnection = getConnection(world, pos, state);

        BlockState newState = super.updateShape(state, direction, neighborState, world, pos, neighborPos);

        newState = newState.setValue(CONNECTION, newConnection);

        return newState;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);
        if (world.isClientSide() && world.getBlockEntity(pos) instanceof BallMillBlockEntity be)
        {
            be.updateFromNeighbour = true;
        }
    }

    private AxisConnection getConnection(LevelAccessor world, BlockPos pos, BlockState state)
    {
        Direction.Axis axis = state.getValue(AXIS);
        boolean plus = world.getBlockState(pos.relative(axis, 1)).is(this);
        boolean minus = world.getBlockState(pos.relative(axis, -1)).is(this);

        return AxisConnection.from(plus, minus);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        var plus = offset(pos, state, 1);
        var minus = offset(pos, state, -1);

        if (world.getBlockEntity(pos) instanceof BallMillBlockEntity be)
        {
            if (world.getBlockEntity(minus) instanceof BallMillBlockEntity be1)
            {
                be.setProgressIncrement(be1.progressIncrement());
            }
            else if (world.getBlockEntity(plus) instanceof BallMillBlockEntity be1)
            {
                be.setProgressIncrement(be1.progressIncrement());
            }
        }

        super.setPlacedBy(world, pos, state, placer, itemStack);
    }

    public static BlockPos offset(BlockPos pos, BlockState state, int i)
    {
        Direction.Axis axis = state.getValue(AXIS);
        return pos.relative(axis, i);
    }

    @Override
    public BigBlockPattern getVolume(BlockState state)
    {
        return patternMap.get(state.getValue(AXIS));
    }

    @Override
    public Int2ObjectMap<Vec3i> getIndexToOffset(BlockState state)
    {
        return offsetMap.get(state.getValue(AXIS));
    }

    @Override
    protected BigBlock2Structure makeStructure(MeatlibBlockSettings settings)
    {
        return new BMStructure(this, settings);
    }

    @Override
    protected BlockState getDefaultStructureState(BlockState parentState)
    {
        return structure.defaultBlockState().setValue(AXIS, parentState.getValue(AXIS));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS);
        builder.add(CONNECTION);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return LivingMachines.BALL_MILL_BE.create(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, LivingMachines.BALL_MILL_BE, null, (world1, pos, state1, blockEntity) -> blockEntity.clientTick(), world);
    }

    public enum AxisConnection implements StringRepresentable
    {
        NONE,
        PLUS,
        MINUS,
        PLUS_MINUS;

        public static AxisConnection from(boolean plus, boolean minus)
        {
            return !(plus || minus) ? NONE : plus && minus ? PLUS_MINUS : plus ? PLUS : MINUS;
        }

        public boolean minus()
        {
            return this == MINUS || this == PLUS_MINUS;
        }

        public boolean plus()
        {
            return this == PLUS || this == PLUS_MINUS;
        }

        @Override
        public String getSerializedName()
        {
            return switch (this)
            {
                case NONE -> "none";
                case PLUS -> "plus";
                case MINUS -> "minus";
                case PLUS_MINUS -> "plus_minus";
            };
        }
    }

    private static class BMStructure extends BigBlock2Structure
    {
        public BMStructure(BigBlock2 parent, Properties settings)
        {
            super(parent, settings);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
        {
            super.createBlockStateDefinition(builder);
            builder.add(AXIS);
        }
    }
}
