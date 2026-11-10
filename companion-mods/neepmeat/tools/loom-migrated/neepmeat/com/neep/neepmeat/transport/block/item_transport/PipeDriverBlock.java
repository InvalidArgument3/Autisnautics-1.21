/*
 * Copyright (c) MeatWheeze 2026.
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

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.util.PipeConnections;
import com.neep.meatlib.util.graph.PipeVertex;
import com.neep.neepmeat.transport.ItemTransport;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import com.neep.neepmeat.transport.item_network.ItemPipeVertex;
import com.neep.neepmeat.transport.screen.PipeDriverScreenHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class PipeDriverBlock extends BaseBlock implements EntityBlock, ItemPipe
{
    public static final BooleanProperty VALID = BooleanProperty.create("valid");
    public static final BooleanProperty HAS_REQUESTS = BooleanProperty.create("has_requests");

    public PipeDriverBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
        this.registerDefaultState(defaultBlockState().setValue(VALID, false).setValue(HAS_REQUESTS, false));
    }

    @Override
    public long insert(Level world, BlockPos pos, BlockState state, Direction direction, ItemInPipe item, TransactionContext transaction)
    {
        return 0;
    }

    @Override
    public boolean isConnected(BlockGetter blockView, BlockPos pos, BlockState state, Direction face)
    {
        return true;
    }

    @Override
    public boolean canItemEnter(ItemVariant variant, long amount, Level world, BlockPos pos, BlockState state, Direction inFace, boolean routed)
    {
        return false;
    }

    @Override
    public boolean canItemLeave(ResourceAmount<ItemVariant> item, Level world, BlockPos pos, BlockState state, Direction outFace, boolean routed)
    {
        return false;
    }

    @Override
    public PipeConnections getConnections(BlockState state, @Nullable Direction forbidden)
    {
        return PipeConnections.ALL.copy();
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify)
    {
        super.onPlace(state, world, pos, oldState, notify);
        if (!world.isClientSide()) world.getBlockEntity(pos, ItemTransport.PIPE_DRIVER_BE).ifPresent(be -> be.getNetwork(null).invalidate());
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        super.neighborChanged(state, world, pos, block, fromPos, notify);
        if (!world.isClientSide()) world.getBlockEntity(pos, ItemTransport.PIPE_DRIVER_BE).ifPresent(be -> be.getNetwork(null).invalidate());
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        super.onRemove(state, world, pos, newState, moved);
        if (world instanceof ServerLevel serverWorld)
        {
            PipeDriverBlockEntity.emitUpdate(serverWorld, pos, ItemTransport.BFS_MAX_DEPTH);
        }
    }

    @Override
    public boolean isSignalSource(BlockState state)
    {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
    {
        return state.getValue(HAS_REQUESTS) ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(VALID, HAS_REQUESTS);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, ItemTransport.PIPE_DRIVER_BE, PipeDriverBlockEntity::serverTick, null, world);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return ItemTransport.PIPE_DRIVER_BE.create(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof PipeDriverBlockEntity be)
        {
//            if (be.getNetwork(null) instanceof RoutingNetworkImpl network)
//            {
//                for (RoutingNetwork.DeferredRequest activeRequest : network.getActiveRequests())
//                {
//                    player.sendMessage(Text.of(activeRequest.toString()),  false);
//                }
//                return ActionResult.SUCCESS;
//            }

            player.openMenu((SimpleScreenHandlerFactory) (s, i, p) -> new PipeDriverScreenHandler(i, be, s));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public PipeVertex getPipeVertex(ServerLevel world, BlockPos pos, BlockState state)
    {
        return ItemPipeVertex.NULL;
    }

    @Override
    public boolean alwaysAutoConnect(BlockState state, Direction face)
    {
        return true;
    }
}