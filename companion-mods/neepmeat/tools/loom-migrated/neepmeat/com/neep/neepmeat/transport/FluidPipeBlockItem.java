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

import com.neep.meatlib.api.item.ScrollableItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.item.FluidComponentItem;
import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import com.neep.neepmeat.transport.fluid_network.ConnectionType;
import com.neep.neepmeat.util.NMMaths;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public class FluidPipeBlockItem extends FluidComponentItem implements ScrollableItem
{
    public FluidPipeBlockItem(Block block, RegistrationContext ctx, ItemSettings itemSettings)
    {
        super(block, ctx, itemSettings.tooltip(new ShiftHidden(1, ChatFormatting.GRAY)));
    }

    @Override
    public void onScroll(Player player, ItemStack stack, InteractionHand hand, double amount)
    {
        if (
                stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof FluidPipeBlock fluidPipeBlock
                && FluidTransport.FLUID_PIPES.contains(fluidPipeBlock)
        )
        {
            int idx = FluidTransport.FLUID_PIPES.indexOf(fluidPipeBlock);
            FluidPipeBlock nextBlock = FluidTransport.FLUID_PIPES.get(
                    NMMaths.wrapIndex(idx + ((int) amount), FluidTransport.FLUID_PIPES.size())
            );

            ScrollableItem.swapStack(player, hand, stack, nextBlock.asItem());
        }
    }

    private static class ShiftHidden implements TooltipSupplier
    {
        private final int lines;
        private final ChatFormatting formatting;

        public ShiftHidden(int lines, ChatFormatting formatting)
        {
            this.lines = lines;
            this.formatting = formatting;
        }

        @Override
        public void apply(ItemStack stack, List<Component> tooltip)
        {
            if (lines != 0 && Screen.hasShiftDown())
            {
                var txt = Component.translatable(FluidTransport.FLUID_PIPE.getDescriptionId() + ".desc").withStyle(formatting);
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

        Storage<FluidVariant> hereStorage = FluidStorage.SIDED.find(world, pos, side);
        Storage<FluidVariant> offsetStorage = FluidStorage.SIDED.find(world, offsetPos, side.getOpposite());

        if ((hereStorage != null || world.getBlockState(pos).getBlock() instanceof FluidPipeBlock)
                && offsetState.getBlock() instanceof FluidPipeBlock fluidPipeBlock
            && !fluidPipeBlock.getConnection(offsetState, side.getOpposite()).isConnected())
        {
            if (!world.isClientSide())
                fluidPipeBlock.connect(world, offsetPos, offsetState, player, side.getOpposite(), ConnectionType.SIDE);

            world.playSound(null, pos, getBlock().getSoundType(state).getPlaceSound(), SoundSource.BLOCKS, 1, 1);
            return InteractionResult.SUCCESS;
        }
        else if (offsetStorage != null && world.getBlockState(pos).getBlock() instanceof FluidPipeBlock fluidPipeBlock)
        {
            fluidPipeBlock.connect(world, pos, state, player, side, ConnectionType.SIDE);
            world.playSound(null, pos, getBlock().getSoundType(state).getPlaceSound(), SoundSource.BLOCKS, 1, 1);
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
//                    || (stack.isOf(this.asItem())
//            && !state.get(getConnection(changeDirection)).isConnected()
//            && !world.getBlockState(pos.offset(changeDirection)).isAir()
//    )
    }
}
