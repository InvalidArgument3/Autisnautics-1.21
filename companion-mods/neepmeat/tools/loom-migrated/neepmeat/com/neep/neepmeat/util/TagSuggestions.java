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

package com.neep.neepmeat.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.ArrayList;
import java.util.List;

public class TagSuggestions
{
    public static final TagSuggestions INSTANCE = new TagSuggestions();

    private boolean dirty = true;
    private final Trie<String, ResourceLocation> trie = new PatriciaTrie<>();
    private Registry<Item> itemRegistry;

    public List<ResourceLocation> get(String prefix)
    {
        if (dirty)
            generate();

        return new ArrayList<>(trie.prefixMap(prefix).values());
    }

    private void generate()
    {
        dirty = false;
        trie.clear();
        itemRegistry.getTagNames().forEach(tag ->
        {
            trie.put(tag.location().toString(), tag.location());
        });
    }

    public static void init()
    {
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) ->
        {
            INSTANCE.itemRegistry = registries.registryOrThrow(BuiltInRegistries.ITEM.key());
        });
    }

    public boolean isValid(ResourceLocation tagId)
    {
        return trie.containsValue(tagId);
    }
}
