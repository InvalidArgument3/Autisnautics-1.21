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

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.util.PipeConnectionsC;
import com.neep.meatlib.util.graph.PipeVertex;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.meatlib.util.PipeConnections;
import com.neep.neepmeat.transport.block.item_transport.entity.RouterBlockEntity;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RouterBlock extends BaseBlock implements EntityBlock, ItemPipe
{
    public RouterBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!world.isClientSide)
        {
            if (world.getBlockEntity(pos) instanceof RouterBlockEntity be)
            {
                player.openMenu(be);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (world instanceof ServerLevel serverWorld)
        {

        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public boolean canItemEnter(ItemVariant variant, long amount, Level world, BlockPos pos, BlockState state, Direction inFace, boolean routed)
    {
        if (world.getBlockEntity(pos) instanceof RouterBlockEntity be)
        {
            Direction output = be.getOutputDirection(variant);
            return inFace != output && output != null;
        }
        return false;
    }

    @Override
    public long insert(Level world, BlockPos pos, BlockState state, Direction direction, ItemInPipe item, TransactionContext transaction)
    {
        if (world.getBlockEntity(pos) instanceof RouterBlockEntity be)
        {
            Direction output = be.getOutputDirection(item.resource());
            if (direction != output && output != null)
            {
                return ItemPipeUtil.EjectOperation.of(world, pos, output).useRouting(false).pipeToAny(item, transaction).amount();
            }
        }
        return 0;
    }

    @Override
    public boolean isConnected(BlockGetter blockView, BlockPos pos, BlockState state, Direction face)
    {
        return true;
    }

    @Override
    public boolean singleOutput()
    {
        return false;
    }

    @Override
    public PipeConnections getConnections(BlockState state, @Nullable Direction forbidden)
    {
        return PipeConnections.ALL.with(forbidden, false);
    }

    @Override
    public boolean prioritise()
    {
        return true;
    }

    @Override
    public boolean alwaysAutoConnect(BlockState state, Direction face)
    {
        return true;
    }

    @Override
    public boolean endPoint()
    {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new RouterBlockEntity(pos, state);
    }

    @Override
    public PipeVertex getPipeVertex(ServerLevel world, BlockPos pos, BlockState state)
    {
        if (world.getBlockEntity(pos) instanceof RouterBlockEntity be)
        {
            return be.getVertex();
        }

        return ItemPipe.super.getPipeVertex(world, pos, state);
    }
}
