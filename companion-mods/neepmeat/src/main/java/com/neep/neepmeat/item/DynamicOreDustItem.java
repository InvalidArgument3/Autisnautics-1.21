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

package com.neep.neepmeat.item;

import com.neep.meatlib.item.TooltipSupplier;
import com.neep.neepmeat.api.processing.OreDustColours;
import com.neep.neepmeat.api.processing.OreDustRegistry;
import com.neep.neepmeat.api.processing.OreFatRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DynamicOreDustItem extends AbstractOreDustItem
{
    public final OreDustRegistry registry;

    public DynamicOreDustItem(TooltipSupplier tooltipSupplier, OreDustRegistry registry, Properties settings)
    {
        super(tooltipSupplier, settings);
        this.registry = registry;
    }

    public int getTint(ItemStack stack, int index)
    {
        return getTint(registry, stack, index);
    }

    public static int getTint(OreDustRegistry registry, ItemStack stack, int index)
    {
        if (index == 1)
        {
            CompoundTag nbt = com.neep.meatlib.util.MiscUtil.itemTag(stack);
            if (nbt != null)
            {
                ResourceLocation entryId = ResourceLocation.tryParse(nbt.getString("ore_item"));
                return OreDustColours.getTint(registry, entryId, true);
            }
        }
        return -1;
    }

    @Override
    public Component getName(ItemStack stack)
    {
        return Component.translatable(this.getDescriptionId(stack), getOreItem(stack).getDescription());
    }

    public Item getOreItem(ItemStack stack)
    {
        if (stack.hasTag())
        {
            ResourceLocation entryId = ResourceLocation.tryParse(com.neep.meatlib.util.MiscUtil.itemTag(stack).getString("ore_item"));
            return BuiltInRegistries.ITEM.get(entryId);
        }

        return Items.AIR;
    }

//    @Override
//    public Identifier getOreItemId(ItemStack stack)
//    {
//        if (stack.hasNbt())
//            return Identifier.tryParse(stack.getNbt().getString("ore_item"));
//
//        return new Identifier("");
//    }

    @Override
    public ItemStack getOutput(ItemStack stack)
    {
        Item input = getOreItem(stack);
        var route = OreFatRegistry.getFromInput(input);
        if (route != null)
            return route.result().toStack(1);

        return ItemStack.EMPTY;
    }
}
