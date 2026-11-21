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

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ItemRegistry
{
    public static final List<Item> REGISTERED_ITEMS = new ArrayList<>();

    /**
     * @return An item whose ID matches the block, hopefully the block's corresponding BlockItem.
     */
    @Nullable
    public static Item getMatchingItem(Block block)
    {
        return BuiltInRegistries.ITEM.getOptional(BuiltInRegistries.BLOCK.getKey(block)).orElse(null);
    }

//    public static Item queue(String namespace, MeatlibItem item)
//    {
//        MeatLib.assertActive(item);
//        if (!(item instanceof Item))
//        {
//            throw new IllegalArgumentException("tried to queue a non-item for item registration");
//        }
//        ITEMS.put(new Identifier(namespace, item.getRegistryName()), (Item) item);
//        return (Item) item;
//    }

//    public static <T extends Item & MeatlibItem> T queue(T item)
//    {
//        MeatLib.assertActive(item);
//        ITEMS.put(new Identifier(MeatLib.CURRENT_NAMESPACE, item.getRegistryName()), item);
//        return item;
//    }

//    public static <T extends Item> T queue(String path, T item)
//    {
//        MeatLib.assertActive(item);
//        ITEMS.put(new Identifier(MeatLib.CURRENT_NAMESPACE, path), item);
//        return item;
//    }

//    public static void flush()
//    {
//        for (Iterator<Map.Entry<Identifier, Item>> it = ITEMS.entrySet().iterator(); it.hasNext();)
//        {
//            Map.Entry<Identifier, Item> entry = it.next();
//
//            // TODO: Remove the jank
//            Registry.register(Registries.ITEM, entry.getKey(), entry.getValue());
//
//            ItemGroup group = ((MeatlibItemExtension) entry.getValue()).meatlib$getItemGroup();
//            if (group != null)
//            {
//                MeatlibItemGroups.add(group, entry.getValue());
//            }
//
//            REGISTERED_ITEMS.add(entry.getValue());
//
//            it.remove();
//        }
//        ITEMS.clear();
//    }
}
