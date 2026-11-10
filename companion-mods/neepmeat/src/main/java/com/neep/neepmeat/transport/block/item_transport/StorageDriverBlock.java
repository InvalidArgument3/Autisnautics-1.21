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

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.transport.ItemTransport;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.block.item_transport.entity.StorageDriverBlockEntity;
import com.neep.neepmeat.transport.screen.PipePriorityScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

public class StorageDriverBlock extends ItemPipeBlock implements ItemPipe
{
    public StorageDriverBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context)
    {
        return super.getShape(state, view, pos, context);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (!world.isClientSide())
        {
            world.getBlockEntity(pos, ItemTransport.STORAGE_BUS_BE).ifPresent(be -> be.update((ServerLevel) world, pos));
        }
    }

    @Override
    protected boolean adjacentAutoConnect(Level world, BlockPos adjPos, BlockState adjState, BlockState hereState, Direction direction)
    {
        if (canConnectTo(hereState, adjState, direction.getOpposite(), world, adjPos, true))
        {
            if (adjState.getBlock() instanceof ItemPipe pipe)
            {
                int adjConnections = pipe.getConnections(adjState, null).size();
                return pipe.alwaysAutoConnect(adjState, direction.getOpposite()) || adjConnections <= 1;
            }
            else
            {
                // Always connect to storage APIs
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canConnectTo(BlockState hereState, BlockState toState, Direction toFace, Level world, BlockPos toPos, boolean newConnection)
    {
        // Always connect to storage APIs
        if (toState.getBlock() instanceof ItemPipe otherPipe)
        {
            return otherPipe.canConnect(world, toPos, toState, toFace)
                    && (newConnection || ItemPipe.isConnectedIn(world, toPos, toState, toFace));
        }
        else
        {
            return canConnectToApi(world, toPos, toState, hereState, toFace);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (player.isShiftKeyDown() && world.getBlockEntity(pos) instanceof StorageDriverBlockEntity be)
        {
            player.openMenu(PipePriorityScreenHandler.open(be));
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean notify)
    {
        super.neighborChanged(state, world, pos, sourceBlock, fromPos, notify);
        if (!world.isClientSide())
        {
            world.getBlockEntity(pos, ItemTransport.STORAGE_BUS_BE).ifPresent(be -> be.update((ServerLevel) world, pos));
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, ItemTransport.STORAGE_BUS_BE, (world1, pos, state1, blockEntity) -> blockEntity.serverTick(world1, pos, state), null, world);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return ItemTransport.STORAGE_BUS_BE.create(pos, state);
    }

    @Override
    public boolean isConnected(BlockGetter blockView, BlockPos pos, BlockState state, Direction face)
    {
        return true;
    }
}