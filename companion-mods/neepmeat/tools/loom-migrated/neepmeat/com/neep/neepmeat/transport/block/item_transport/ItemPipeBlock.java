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

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.util.PipeGraphUtil;
import com.neep.meatlib.util.graph.PipeVertex;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.ItemPipeBlockItem;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.api.pipe.ItemPipeConnectable;
import com.neep.neepmeat.transport.api.pipe.PrecisePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.fluid_network.ConnectionType;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import com.neep.neepmeat.transport.item_network.ItemPipeGraphHelper;
import com.neep.neepmeat.transport.item_network.ItemPipeVertexImpl;
import com.neep.meatlib.util.MiscUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ItemPipeBlock extends PrecisePipeBlock implements EntityBlock, ItemPipe
{
    public ItemPipeBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings.factory(ItemPipeBlockItem::new), settings);
    }

    protected static boolean matches(ItemStack stack)
    {
        return stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ItemPipeBlock;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        ItemPipe.onStateReplaced(world, pos, state, newState);

        if (!state.is(newState.getBlock()))
        {
            if (world.getBlockEntity(pos) instanceof ItemPipeBlockEntity be)
            {
                be.getCallbacks().invalidate();
                be.dropItems();
                be.getCache().update();
            }
            world.removeBlockEntity(pos);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean notify)
    {
        if (world instanceof ServerLevel serverWorld)
        {
            if (world.getBlockEntity(pos) instanceof ItemPipeBlockEntity be)
            {
                be.getCache().neighbourUpdate(world, fromPos, sourceBlock);
                be.getCallbacks().invalidate();
            }

            BlockPos subtracted = fromPos.subtract(pos);
            Direction direction = Direction.fromDelta(subtracted.getX(), subtracted.getY(), subtracted.getZ());

            BlockState nextState;
            if (direction == null)
                nextState = state;
            else
                nextState = updateShape(state, direction, world.getBlockState(fromPos), world, pos, fromPos);

            // The storage check ensures that connections are updated when no blockstate change occurs, but leads to lots of unnecessary updates
//            if (!state.equals(nextState) || ItemStorage.SIDED.find(world, fromPos, direction != null ? direction.getOpposite() : null) != null)
            if (!state.equals(nextState))
            {
                ItemPipeGraphHelper.find(serverWorld, pos, state).ifPresent(h -> h.updateConnectionChange(state, nextState));
            }
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
//        BlockState updatedState = enforceApiConnections(world, pos, state);
//        world.setBlockState(pos, updatedState,  Block.NOTIFY_ALL);

        if (world instanceof ServerLevel serverWorld)
        {
            ItemPipeGraphHelper.find(serverWorld, pos, state).ifPresent(h -> h.updateHiddenConnections(state));
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        if (state.getValue(WATERLOGGED))
        {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        // I know how manky this logic is, but my brain refuses to make it more readable.

        boolean alreadyConnected = getConnection(state, direction).isConnected();
        boolean canConnect = canConnectTo(state, neighborState, direction.getOpposite(), (Level) world, neighborPos, alreadyConnected);

        boolean otherIsPipe;
        boolean otherConnected;
        if (neighborState.getBlock() instanceof ItemPipeBlock neighborPipe)
        {
            otherConnected = neighborPipe.getConnection(neighborState, direction.getOpposite()).isConnected();
            otherIsPipe = true;
        }
        else
        {
            otherConnected = canConnect && alreadyConnected;
            otherIsPipe = false;
        }

        ConnectionType finalConnection;
        if (otherConnected || (!otherIsPipe && canConnect))
            finalConnection = ConnectionType.SIDE;
        else
            finalConnection = ConnectionType.NONE;

        return state.setValue(getConnection(direction), finalConnection);
    }

    @Override
    public boolean canConnectTo(BlockState hereState, BlockState toState, Direction toFace, Level world, BlockPos toPos, boolean newConnection)
    {
        if (toState.getBlock() instanceof ItemPipe otherPipe)
        {
            return otherPipe.canConnect(world, toPos, toState, toFace)
                    && (newConnection || ItemPipe.isConnectedIn(world, toPos, toState, toFace));
        }
        else return (newConnection || toState.getBlock() instanceof ItemPipeConnectable)
                && canConnectToApi(world, toPos, toState, hereState, toFace);
    }

    @Override
    protected boolean adjacentAutoConnect(Level world, BlockPos adjPos, BlockState adjState, BlockState hereState, Direction direction)
    {
        boolean canConnect = canConnectTo(hereState, adjState, direction.getOpposite(), world, adjPos, true);
        if (canConnect)
        {
            if (adjState.getBlock() instanceof ItemPipe pipe)
            {
                int adjConnections = pipe.getConnections(adjState, null).size();
                return pipe.alwaysAutoConnect(adjState, direction.getOpposite()) || adjConnections <= 1;
            }
            else if (adjState.getBlock() instanceof ItemPipeConnectable)
            {
                return true;
            }
        }

        return false;
    }

    protected boolean canConnectToApi(Level world, BlockPos pos, BlockState state, BlockState hereState, Direction direction)
    {
        return ItemStorage.SIDED.find(world, pos, direction) != null;
    }

    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (player.getItemInHand(hand).is(Items.STICK))
        {
            if (world instanceof ServerLevel serverWorld && world.getBlockEntity(pos) instanceof ItemPipeBlockEntity be)
            {
                if (be.getVertex() instanceof ItemPipeVertexImpl ipv)
                {
                    if (!ipv.canSimplify())
                    {
                        player.sendSystemMessage(Component.nullToEmpty("Vertex"));
                        for (var v : ipv.getAdjVertices())
                        {
                            PipeGraphUtil.jankParticles1(serverWorld, v, true);
                        }
                    }
                    else
                    {
                        player.sendSystemMessage(Component.nullToEmpty("No vertex"));
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public void onConnectionUpdate(Level world, BlockState state, BlockState newState, BlockPos pos, Player entity)
    {
        if (world.getBlockEntity(pos) instanceof ItemPipeBlockEntity be)
            be.getCallbacks().invalidate();

        if (world instanceof ServerLevel serverWorld)
        {
            ItemPipeGraphHelper.find(serverWorld, pos, state).ifPresent(h -> h.updateConnectionChange(state, newState));
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.ITEM_PIPE.create(pos, state);
    }

    @Override
    public boolean isConnected(BlockGetter blockView, BlockPos pos, BlockState state, Direction face)
    {
        return state.getValue(DIR_TO_CONNECTION.get(face)).isConnected();
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.ITEM_PIPE, (world1, pos, state1, blockEntity) -> blockEntity.serverTick(world1, pos, state1), null, world);
    }

    @Override
    public long insert(Level world, BlockPos pos, BlockState state, Direction direction, ItemInPipe item, TransactionContext transaction)
    {
        if (world.getBlockEntity(pos) instanceof ItemPipeBlockEntity be)
        {
            return be.insert(item, world, state, pos, direction, transaction);
        }
        return 0;
    }

    @Override
    public Direction getOutputDirection(ItemInPipe item, BlockPos pos, BlockState state, Level world, Direction in, @Nullable BlockEntity beIn, boolean overridePathNode, TransactionContext transaction)
    {
//        Set<Direction> connections = ((ItemPipe) state.getBlock()).getConnections(state, direction -> direction != in);
        var connections = ((ItemPipe) state.getBlock()).getConnections(state, null);

        Direction out = item.getPreferredOutputDirection(state, in, this, overridePathNode);
        if (out != null && connections.contains(out))
            return out;

        EnumSet<Direction> excluded = EnumSet.noneOf(Direction.class);

        if (beIn instanceof ItemPipeBlockEntity be)
        {
            int iterations = 0; // Ensure that the loop cannot run infinitely if something weird happens.

            int initialOut = be.getCurrentOutput(connections);
            int outId;
            do
            {
                iterations++;

                outId = be.nextOutput(connections);
                Direction outDir = Direction.values()[outId];
                if (outDir == in)
                    continue;

                var cache = be.getStorageCache(outDir);
                BlockState adjState = world.getBlockState(cache.getPos());
                ItemPipe pipe = ItemPipe.findItemPipe(adjState);
                if (pipe != null && pipe.prioritise())
                {
                    boolean inserted = pipe.canItemEnter(item.resource(), item.amount(), world, cache.getPos(), adjState, outDir.getOpposite(), item.hasRoute());
                    if (inserted)
                        return outDir;

                    excluded.add(outDir);
                    continue;
                }

                Storage<ItemVariant> storage = cache.find(outDir.getOpposite());
                if (storage != null)
                {
                    long inserted = MeatlibStorageUtil.simulateInsert(storage, item.resource(), item.amount(), transaction);

                    if (inserted >= item.amount())
                        return outDir;

                    excluded.add(outDir);
                }

            }
            while (outId != initialOut && iterations <= 6);

            // No pipes are being prioritised, so resort to blind round robin.
            iterations = 0;
            be.setOutput(initialOut);
            do
            {
                iterations++;
                outId = be.nextOutput(connections);
                var outDir = Direction.values()[outId];
                if (excluded.contains(outDir))
                    continue;
                if (outDir == in)
                    continue;

                return outDir;
            }
            while (outId != initialOut && iterations <= 6);
        }

        connections.unset(in);
        connections.unsetAll(excluded);
        if (!connections.isEmpty())
            out = connections.get(world.getRandom().nextInt(connections.size()));
        else
            out = in;
        return out;
    }

    @Override
    public boolean supportsRouting()
    {
        return true;
    }

    @Override
    public PipeVertex getPipeVertex(ServerLevel world, BlockPos pos, BlockState state)
    {
        if (world.getBlockEntity(pos) instanceof ItemPipeBlockEntity be)
        {
            return be.getVertex();
        }

        return null;
    }

    @Override
    public boolean endPoint()
    {
        return false;
    }
}
