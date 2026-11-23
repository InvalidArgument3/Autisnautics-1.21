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

package com.neep.neepmeat.plc.block;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.neepmeat.plc.block.entity.ProcessInterfaceBlockEntity;
import com.neep.neepmeat.transport.ItemTransport;
import com.neep.neepmeat.transport.api.pipe.ItemPipeConnectable;
import com.neep.neepmeat.transport.screen.ProcessInterfaceScreenHandler;
import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.transfer.ItemStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ProcessInterfaceBlock extends BaseFacingBlock implements EntityBlock, ItemPipeConnectable
{
    public ProcessInterfaceBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return ItemTransport.PROCESS_INTERFACE_BE.create(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Direction facing = context.getClickedFace().getOpposite();

        BlockPos offset = context.getClickedPos().relative(facing);

        Storage<ItemVariant> storage = ItemStorage.SIDED.find(context.getLevel(), offset, facing.getOpposite());

        if (storage != null)
        {
            return defaultBlockState().setValue(FACING, facing);
        }

        return this.defaultBlockState().setValue(FACING, (context.getPlayer() != null && context.getPlayer().isShiftKeyDown())
                ? context.getClickedFace() : context.getClickedFace().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof ProcessInterfaceBlockEntity be)
        {
            if (!world.isClientSide() && player.isShiftKeyDown())
            {
                var requests = be.getRequests();
                player.displayClientMessage(Component.nullToEmpty("Cleared %s requests".formatted(requests.size())), false);
                requests.clear();
            }
            else if (!world.isClientSide())
            {
                player.openMenu((SimpleScreenHandlerFactory) (syncId, playerInventory, player1) ->
                        new ProcessInterfaceScreenHandler(playerInventory, be.getResult(), be.getCardInventory(), be, syncId));
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(this) && world.getBlockEntity(pos) instanceof ProcessInterfaceBlockEntity be)
        {
            Containers.dropContents(world, pos, be.getResult());
        }

        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public net.minecraft.world.level.block.state.BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player)
    {
        if (world.getBlockEntity(pos) instanceof ProcessInterfaceBlockEntity be)
        {
            if (!world.isClientSide() && player.isCreative() && !be.getCardInventory().isEmpty())
            {
                MiscUtil.dropBlockWithNbt(world, this, pos, be);
            }
        }
        super.playerWillDestroy(world, pos, state, player);
    
        return state;
    }
}
