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

package com.neep.meatlib.registry;

import com.neep.meatlib.item.MeatlibItemExtension;
import com.neep.meatlib.util.MeatlibItemGroups;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

@FunctionalInterface
public interface SelfRegistrable
{
    void register(ResourceLocation id);

    static SelfRegistrable ofItem(Item item)
    {
        return id -> registerItem(id, item);
    }

    static SelfRegistrable ofBlock(Block block)
    {
        return id -> registerBlock(id, block);
    }

    static void registerItem(ResourceLocation id, Item item)
    {
        Registry.register(BuiltInRegistries.ITEM, id, item);

        // Jank handling
        ItemRegistry.REGISTERED_ITEMS.add(item);

        CreativeModeTab group = ((MeatlibItemExtension) item).meatlib$getItemGroup();
        if (group != null)
        {
            MeatlibItemGroups.add(group, item::getDefaultInstance);
        }
    }

    static void registerBlock(ResourceLocation id, Block block)
    {
        // Jank handling
        BlockRegistry.REGISTERED_BLOCKS.add(block);

        Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    @FunctionalInterface
    interface PathProcessor extends Object2ObjectFunction<String, String>
    {
        @Override
        default String get(Object o)
        {
            return apply((String) o);
        }

        @Override
        String apply(String key);
    }
}
