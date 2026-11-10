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

package com.neep.neepmeat.transport.block.item_transport;

import com.mojang.serialization.MapCodec;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.transfer.TransactionContext;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.api.pipe.ItemPipeConnectable;
import com.neep.neepmeat.transport.api.pipe.PrecisePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.fluid_network.ConnectionType;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import com.neep.neepmeat.transport.item_network.ItemPipeGraphHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

public class ItemPipeBlock extends PrecisePipeBlock implements EntityBlock, ItemPipe {
    public static final MapCodec<ItemPipeBlock> CODEC = simpleCodec(ItemPipeBlock::new);

    public ItemPipeBlock(BlockBehaviour.Properties properties) {
        this(null, new ItemSettings(), properties);
    }

    public ItemPipeBlock() {
        this(BlockBehaviour.Properties.of().strength(0.8F).noOcclusion());
    }

    public ItemPipeBlock(RegistrationContext ctx, ItemSettings itemSettings, BlockBehaviour.Properties properties) {
        super(ctx, itemSettings, properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    /** Joins every adjacent item pipe. {@code setBlock} does not run placement, so graphs refresh this themselves. */
    public void joinComponent(Level level, BlockPos origin) {
        java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
        java.util.HashSet<BlockPos> seen = new java.util.HashSet<>();
        queue.add(origin);
        seen.add(origin);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            if (!(level.getBlockState(pos).getBlock() instanceof ItemPipeBlock)) {
                continue;
            }
            joinNeighbours(level, pos);
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (seen.add(next) && level.getBlockState(next).getBlock() instanceof ItemPipeBlock) {
                    queue.add(next);
                }
            }
        }
    }

    public void joinNeighbours(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        for (Direction direction : Direction.values()) {
            BlockPos neighbourPos = pos.relative(direction);
            BlockState neighbour = level.getBlockState(neighbourPos);
            if (!canConnectTo(state, neighbour, direction.getOpposite(), level, neighbourPos, true)) {
                continue;
            }
            state = state.setValue(getConnection(direction), ConnectionType.SIDE);
            if (neighbour.getBlock() instanceof ItemPipeBlock neighbourPipe) {
                BlockState joined = neighbour.setValue(neighbourPipe.getConnection(direction.getOpposite()), ConnectionType.SIDE);
                if (!joined.equals(neighbour)) {
                    level.setBlock(neighbourPos, joined, Block.UPDATE_CLIENTS);
                }
            }
        }
        if (!state.equals(level.getBlockState(pos))) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean canConnectTo(BlockState hereState, BlockState toState, Direction toFace, Level world, BlockPos toPos, boolean newConnection) {
        if (toState.getBlock() instanceof ItemPipe otherPipe) {
            return otherPipe.canConnect(world, toPos, toState, toFace)
                    && (newConnection || ItemPipe.isConnectedIn(world, toPos, toState, toFace));
        }
        return (newConnection || toState.getBlock() instanceof ItemPipeConnectable)
                && canConnectToApi(world, toPos, toState, hereState, toFace);
    }

    protected boolean canConnectToApi(Level world, BlockPos pos, BlockState state, BlockState hereState, Direction direction) {
        return world.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction) != null;
    }

    @Override
    protected boolean adjacentAutoConnect(Level world, BlockPos adjPos, BlockState adjState, BlockState hereState, Direction direction) {
        boolean canConnect = canConnectTo(hereState, adjState, direction.getOpposite(), world, adjPos, true);
        if (!canConnect) {
            return false;
        }
        if (adjState.getBlock() instanceof ItemPipe pipe) {
            int adjConnections = pipe.getConnections(adjState, null).size();
            return pipe.alwaysAutoConnect(adjState, direction.getOpposite()) || adjConnections <= 1;
        }
        return adjState.getBlock() instanceof ItemPipeConnectable;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        boolean alreadyConnected = getConnection(state, direction).isConnected();
        // Game tests place pipes with setBlock, which does not run getStateForPlacement.
        // A neighbouring item pipe is treated as a new connection so the line still joins.
        boolean connectAsNew = alreadyConnected || neighborState.getBlock() instanceof ItemPipe;
        boolean canConnect = world instanceof Level level && canConnectTo(state, neighborState, direction.getOpposite(), level, neighborPos, connectAsNew);
        boolean otherIsPipe = neighborState.getBlock() instanceof ItemPipeBlock neighborPipe;
        boolean otherConnected = otherIsPipe
                ? ((ItemPipeBlock) neighborState.getBlock()).getConnection(neighborState, direction.getOpposite()).isConnected()
                : canConnect && alreadyConnected;
        ConnectionType finalConnection = otherConnected || (!otherIsPipe && canConnect) ? ConnectionType.SIDE : ConnectionType.NONE;
        return state.setValue(getConnection(direction), finalConnection);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean notify) {
        super.neighborChanged(state, world, pos, sourceBlock, fromPos, notify);
        if (!(world instanceof ServerLevel server) || !(world.getBlockEntity(pos) instanceof ItemPipeBlockEntity be)) {
            return;
        }
        BlockPos subtracted = fromPos.subtract(pos);
        Direction direction = Direction.fromDelta(subtracted.getX(), subtracted.getY(), subtracted.getZ());
        BlockState nextState = direction == null ? state : updateShape(state, direction, world.getBlockState(fromPos), world, pos, fromPos);
        if (!state.equals(nextState)) {
            ItemPipeGraphHelper.find(server, pos, state).ifPresent(helper -> helper.updateConnectionChange(state, nextState));
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ItemPipeBlockEntity be) {
            be.dropStored();
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public boolean isConnected(Level world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(DIR_TO_CONNECTION.get(face)).isConnected();
    }

    @Override
    public long insert(Level world, BlockPos pos, BlockState state, Direction direction, ItemInPipe item, TransactionContext transaction) {
        if (world.getBlockEntity(pos) instanceof ItemPipeBlockEntity be) {
            ItemStack stack = item.toItemStack();
            return be.insert(stack, direction);
        }
        return 0;
    }

    @Override
    public Direction getOutputDirection(ItemInPipe item, BlockPos pos, BlockState state, Level world, Direction in, @Nullable BlockEntity be, boolean overridePathNode, TransactionContext transaction) {
        return ItemPipe.super.getOutputDirection(item, pos, state, world, in, be, overridePathNode, transaction);
    }

    @Override
    public boolean supportsRouting() {
        return true;
    }

    @Override
    public boolean endPoint() {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemPipeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof ItemPipeBlockEntity pipe) {
                pipe.serverTick();
            }
        };
    }
}
