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

package com.neep.neepmeat.transport;

import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.fluid_network.ConnectionType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public class ItemPipeBlockItem extends BaseBlockItem
{
    public ItemPipeBlockItem(Block block, RegistrationContext ctx, ItemSettings itemSettings)
    {
        super(block, ctx, itemSettings.tooltip(new ShiftHidden(1, ChatFormatting.GRAY)));
    }

    private record ShiftHidden(int lines, ChatFormatting formatting) implements TooltipSupplier
    {
        @Override
        public void apply(ItemStack stack, List<Component> tooltip)
        {
            if (lines != 0 && Screen.hasShiftDown())
            {
                var txt = Component.translatable(ItemTransport.ITEM_PIPE.getDescriptionId() + ".desc").withStyle(formatting);
                TooltipSupplier.wrapLines(tooltip, txt);
            }
            else
            {
                TooltipSupplier.shiftForInfo(tooltip);
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();

        Direction side = context.getClickedFace();
        BlockPos offsetPos = context.getClickedPos().relative(side);
        BlockState offsetState = world.getBlockState(offsetPos);
        BlockState state = world.getBlockState(pos);

        Storage<ItemVariant> hereStorage = ItemStorage.SIDED.find(world, pos, side);
        Storage<ItemVariant> offsetStorage = ItemStorage.SIDED.find(world, offsetPos, side.getOpposite());

        if ((hereStorage != null || world.getBlockState(pos).getBlock() instanceof ItemPipeBlock)
                && offsetState.getBlock() instanceof ItemPipeBlock block
            && !block.getConnection(offsetState, side.getOpposite()).isConnected())
        {
            if (!world.isClientSide())
                block.connect(world, offsetPos, offsetState, player, side.getOpposite(), ConnectionType.SIDE);

            world.playSound(null, pos, getBlock().getSoundType(state).getPlaceSound(), SoundSource.BLOCKS, 1, 1);
            return InteractionResult.SUCCESS;
        }
        else if (offsetStorage != null && world.getBlockState(pos).getBlock() instanceof ItemPipeBlock block)
        {
            block.connect(world, pos, state, player, side, ConnectionType.SIDE);
            world.playSound(null, pos, getBlock().getSoundType(state).getPlaceSound(), SoundSource.BLOCKS, 1, 1);
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }
}
