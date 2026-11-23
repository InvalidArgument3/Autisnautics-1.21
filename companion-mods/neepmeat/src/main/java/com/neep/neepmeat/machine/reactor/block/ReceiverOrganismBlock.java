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

package com.neep.neepmeat.machine.reactor.block;
import net.minecraft.world.item.Item;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.machine.reactor.ReceiverOrganismStructure;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class ReceiverOrganismBlock extends BaseBlock implements ReceiverOrganismStructure
{
    private final EnumMap<Property, PropertyValue> properties;

    public ReceiverOrganismBlock(RegistrationContext ctx, Map<Property, PropertyValue> properties, Properties settings)
    {
        super(ctx, ItemSettings.block().factory(ReceiverOrganismBlockItem::new), settings);
        this.properties = new EnumMap<>(properties);
    }

    @Override
    public EnumMap<Property, PropertyValue> getProperties()
    {
        return properties;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext world, List<Component> tooltip, TooltipFlag options)
    {
        super.appendHoverText(stack, world, tooltip, options);

        if (!Screen.hasShiftDown())
        {
            tooltip.add(NeepMeat.translationKey("screen", "receiver_organism.block_hold_shift").withStyle(ChatFormatting.RED));
        }
        else
        {
            tooltip.add(NeepMeat.translationKey("screen", "receiver_organism.block").withStyle(ChatFormatting.RED));
            for (var entry : properties.entrySet())
            {
                MutableComponent base = Component.literal("   ")
                        .append(NeepMeat.translationKey("screen", "receiver_organism." + entry.getKey().name().toLowerCase(Locale.ROOT))
                                .withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(": ").withStyle(ChatFormatting.GOLD))
                        .append(entry.getKey().format(entry.getValue()))
                        .append(Component.literal(" (" + entry.getValue().function().name + ")"))
                        ;
                tooltip.add(base);
            }
        }
    }

    public static class ReceiverOrganismBlockItem extends BaseBlockItem
    {
        public ReceiverOrganismBlockItem(Block block, RegistrationContext ctx, ItemSettings itemSettings)
        {
            super(block, ctx, itemSettings);
        }

        @Override
        public void appendHoverText(ItemStack itemStack, Item.TooltipContext world, List<Component> tooltip, TooltipFlag tooltipContext)
        {
            super.appendHoverText(itemStack, world, tooltip, tooltipContext);
            getBlock().appendHoverText(itemStack, world, tooltip, tooltipContext);
        }
    }
}
