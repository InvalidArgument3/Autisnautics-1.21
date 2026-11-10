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

package com.neep.meatlib.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class MeatlibItemGroups
{
    private static final Multimap<CreativeModeTab, Supplier<ItemStack>> GROUPS = ArrayListMultimap.create();

    public static void add(CreativeModeTab group, Item value)
    {
        add(group, value.getDefaultInstance());
    }

    public static void add(CreativeModeTab group, ItemStack value)
    {
        GROUPS.put(group, () -> value);
    }

    public static void add(CreativeModeTab group, Supplier<ItemStack> value)
    {
        GROUPS.put(group, value);
    }

    public static void init()
    {
    }

    public static class SpecialEntryCollector implements CreativeModeTab.DisplayItemsGenerator
    {
        private final List<CreativeModeTab.DisplayItemsGenerator> extra = new java.util.ArrayList<>();

        public void add(CreativeModeTab.DisplayItemsGenerator collector)
        {
            extra.add(collector);
        }

        @Override
        public void accept(CreativeModeTab.ItemDisplayParameters displayContext, CreativeModeTab.Output entries)
        {
            extra.forEach(collector -> collector.accept(displayContext, entries));
        }
    }
}
