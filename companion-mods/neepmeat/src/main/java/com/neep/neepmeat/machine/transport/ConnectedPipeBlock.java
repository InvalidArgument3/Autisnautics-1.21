/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port of the connection half of PrecisePipeBlock: six connection
 * properties, the upstream arm shapes, and waterlogging. Routing graphs are separate.
 */

package com.neep.neepmeat.machine.transport;

import com.neep.neepmeat.transport.ConnectionType;
import com.neep.neepmeat.transport.PipeProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class ConnectedPipeBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    private static final VoxelShape CENTRE = Block.box(4, 4, 4, 12, 12, 12);
    private static final VoxelShape NORTH = Block.box(4, 4, 0, 12, 12, 5);
    private static final VoxelShape EAST = Block.box(11, 4, 4, 16, 12, 12);
    private static final VoxelShape SOUTH = Block.box(4, 4, 11, 12, 12, 16);
    private static final VoxelShape WEST = Block.box(0, 4, 4, 5, 12, 12);
    private static final VoxelShape UP = Block.box(4, 11, 4, 12, 16, 12);
    private static final VoxelShape DOWN = Block.box(4, 0, 4, 12, 5, 12);

    protected ConnectedPipeBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(PipeProperties.NORTH, ConnectionType.NONE)
                .setValue(PipeProperties.EAST, ConnectionType.NONE)
                .setValue(PipeProperties.SOUTH, ConnectionType.NONE)
                .setValue(PipeProperties.WEST, ConnectionType.NONE)
                .setValue(PipeProperties.UP, ConnectionType.NONE)
                .setValue(PipeProperties.DOWN, ConnectionType.NONE)
                .setValue(BlockStateProperties.WATERLOGGED, false));
    }

    protected abstract boolean connectsTo(LevelAccessor level, BlockPos neighbourPos, BlockState neighbour, Direction fromNeighbour);

    public void refreshConnections(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        for (Direction direction : Direction.values()) {
            BlockPos neighbourPos = pos.relative(direction);
            boolean connected = connectsTo(level, neighbourPos, level.getBlockState(neighbourPos), direction.getOpposite());
            state = state.setValue(PipeProperties.of(direction), connected ? ConnectionType.SIDE : ConnectionType.NONE);
        }
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PipeProperties.NORTH, PipeProperties.EAST, PipeProperties.SOUTH, PipeProperties.WEST,
                PipeProperties.UP, PipeProperties.DOWN, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        BlockPos pos = context.getClickedPos();
        for (Direction direction : Direction.values()) {
            BlockPos neighbourPos = pos.relative(direction);
            boolean connected = connectsTo(context.getLevel(), neighbourPos, context.getLevel().getBlockState(neighbourPos), direction.getOpposite());
            state = state.setValue(PipeProperties.of(direction), connected ? ConnectionType.SIDE : ConnectionType.NONE);
        }
        return state.setValue(BlockStateProperties.WATERLOGGED, context.getLevel().getFluidState(pos).getType() == Fluids.WATER);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        boolean connected = connectsTo(level, neighbourPos, neighbour, direction.getOpposite());
        return state.setValue(PipeProperties.of(direction), connected ? ConnectionType.SIDE : ConnectionType.NONE);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CENTRE;
        if (state.getValue(PipeProperties.NORTH).connected()) shape = Shapes.or(shape, NORTH);
        if (state.getValue(PipeProperties.EAST).connected()) shape = Shapes.or(shape, EAST);
        if (state.getValue(PipeProperties.SOUTH).connected()) shape = Shapes.or(shape, SOUTH);
        if (state.getValue(PipeProperties.WEST).connected()) shape = Shapes.or(shape, WEST);
        if (state.getValue(PipeProperties.UP).connected()) shape = Shapes.or(shape, UP);
        if (state.getValue(PipeProperties.DOWN).connected()) shape = Shapes.or(shape, DOWN);
        return shape;
    }
}
