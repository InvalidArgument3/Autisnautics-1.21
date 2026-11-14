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

package com.neep.neepmeat.block;

import com.neep.neepmeat.transport.ConnectionType;
import com.neep.neepmeat.transport.PipeProperties;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class DataCableBlock extends Block implements DataCable {
    public DataCableBlock(Properties properties) {
        super(properties.noOcclusion());
        BlockState state = defaultBlockState();
        for (Direction direction : Direction.values()) {
            state = state.setValue(PipeProperties.of(direction), ConnectionType.NONE);
        }
        registerDefaultState(state);
    }

    @Override
    public boolean checkAdjacentConnection() {
        return false;
    }

    @Override
    public boolean connectInDirection(BlockGetter world, BlockPos pos, BlockState state, Direction direction) {
        return state.getValue(PipeProperties.of(direction)).connected();
    }

    @Override
    public boolean isConnected(Level world, BlockPos pos, BlockState state, Direction direction, boolean outgoing) {
        return state.getValue(PipeProperties.of(direction)).connected();
    }

    public BlockState withConnections(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockState next = state;
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = pos.relative(direction);
            BlockState neighbourState = level.getBlockState(neighbour);
            boolean connect = neighbourState.getBlock() instanceof DataCable cable
                    && cable.connectInDirection(level, neighbour, neighbourState, direction.getOpposite());
            next = next.setValue(PipeProperties.of(direction), connect ? ConnectionType.SIDE : ConnectionType.NONE);
        }
        return next;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (!level.isClientSide() && !state.is(old.getBlock())) {
            BlockState connected = withConnections(level, pos, state);
            if (connected != state) {
                level.setBlock(pos, connected, Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (level instanceof Level world && !world.isClientSide()) {
            return withConnections(level, pos, state);
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PipeProperties.NORTH, PipeProperties.SOUTH, PipeProperties.EAST, PipeProperties.WEST, PipeProperties.UP, PipeProperties.DOWN);
    }
}
