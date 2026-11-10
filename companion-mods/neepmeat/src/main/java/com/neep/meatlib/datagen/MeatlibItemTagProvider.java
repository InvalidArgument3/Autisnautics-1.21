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

package com.neep.meatlib.datagen;

import com.neep.meatlib.block.MeatlibBlockExtension;
import com.neep.meatlib.item.MeatlibItemExtension;
import com.neep.meatlib.registry.ItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import java.util.concurrent.CompletableFuture;

public class MeatlibItemTagProvider extends FabricTagProvider.ItemTagProvider
{
    public MeatlibItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture)
    {
        super(output, registriesFuture);
    }

    protected void addTags(HolderLookup.Provider arg)
    {
//        Multimap<TagKey<Item>, Item> map = HashMultimap.create();
//        ItemRegistry.REGISTERED_ITEMS.stream()
//                .filter(item -> item instanceof MeatlibItem)
//                .forEach(item -> ((MeatlibItem) item).appendTags(tag -> map.put(tag, item)));

        for (Item entry : ItemRegistry.REGISTERED_ITEMS)
        {
            MeatlibBlockExtension.TagConsumer<Item> consumer = t -> tag(t).add(entry);
            ((MeatlibItemExtension) entry).meatlib$appendTags(consumer);
        }

//        map.asMap().forEach((tag, items) ->
//        {
//            var builder = getOrCreateTagBuilder(tag);
//            items.forEach(builder::add);
//        });
    }
}
