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
import com.neep.neepmeat.machine.transport.ConnectedPipeBlock;
import com.neep.neepmeat.machine.transport.DeferredRequest;
import com.neep.neepmeat.machine.transport.FilterEntry;
import com.neep.neepmeat.machine.transport.FilterPipeMenu;
import com.neep.neepmeat.machine.transport.NetworkCrafterBlockEntity;
import com.neep.neepmeat.machine.transport.StorageBusBlock;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.PumpBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import com.neep.neepmeat.transport.block.fluid_transport.machine.PumpBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.FilterItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.MergePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.PipeDriverBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.meatlib.util.graph.PipeVertex;
import com.neep.neepbus.block.NeepBusProvider;
import com.neep.neepmeat.transport.ItemTransport;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.block.item_transport.entity.RequesterBlockEntity;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import com.neep.neepmeat.transport.screen.RequesterScreenHandler;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.TransactionContext;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RequesterBlock extends BaseFacingBlock implements EntityBlock, NeepBusProvider, DataCable, ItemPipe
{
    public RequesterBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return ItemTransport.REQUESTER_BE.create(pos, state);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
        if (world.getBlockEntity(pos) instanceof RequesterBlockEntity be)
        {
            be.getCache().neighbourUpdate(world, sourcePos, sourceBlock);

            boolean redstone = world.hasNeighborSignal(pos);
            if (be.prevHigh != redstone)
            {
                if (!be.prevHigh && redstone)
                    be.request();

                be.setPrevHigh(redstone);
            }
        }

        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (world.getBlockEntity(pos) instanceof RequesterBlockEntity be)
        {
            be.getCache().update();
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof RequesterBlockEntity be)
        {
            if (!world.isClientSide())
            {
                player.openMenu((SimpleScreenHandlerFactory) (s, i, p) ->
                        new RequesterScreenHandler(i, s, be, be.getFilter()));
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {

    }

    @Override
    public boolean canItemEnter(ItemVariant variant, long amount, Level world, BlockPos pos, BlockState state, Direction inFace, boolean routed)
    {
        if (inFace != state.getValue(FACING))
        {
            return routed;
//            if (world.getBlockEntity(pos) instanceof RequesterBlockEntity be)
//            {
//                return be.matches(variant, amount);
//            }
        }

        return false;
    }

    @Override
    public boolean canItemLeave(ResourceAmount<ItemVariant> item, Level world, BlockPos pos, BlockState state, Direction outFace, boolean routed)
    {
        return false; // This will hopefully prevent unrelated routes from being created involving this block
    }

    @Override
    public long insert(Level world, BlockPos pos, BlockState state, Direction direction, ItemInPipe item, TransactionContext transaction)
    {
        Direction facing = state.getValue(FACING);
        if (direction == facing)
            return 0;

        if (world.getBlockEntity(pos) instanceof RequesterBlockEntity be)
        {
            return ItemPipeUtil.EjectOperation.of(world, pos, facing)
                    .useRouting(false)
                    .pipeToAny(item, transaction)
                    .amount();
        }

        return 0;
    }

    @Override
    public PipeVertex getPipeVertex(ServerLevel world, BlockPos pos, BlockState state)
    {
        if (world.getBlockEntity(pos) instanceof RequesterBlockEntity be)
        {
            return be.getVertex();
        }

        return ItemPipe.super.getPipeVertex(world, pos, state);
    }

    @Override
    public boolean endPoint()
    {
        return false;
    }

    @Override
    public boolean supportsRouting()
    {
        return true;
    }

    @Override
    public boolean isConnected(BlockGetter blockView, BlockPos pos, BlockState state, Direction face)
    {
        return true;
    }

    @Override
    public boolean alwaysAutoConnect(BlockState state, Direction face)
    {
        return true;
    }
}
