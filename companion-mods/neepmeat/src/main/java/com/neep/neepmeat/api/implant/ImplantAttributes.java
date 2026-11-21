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

package com.neep.neepmeat.api.implant;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ImplantAttributes
{
    private static final Map<ResourceLocation, Entry> NAMES = Maps.newHashMap();

    public static void register(ResourceLocation id, Entry entry)
    {
        NAMES.put(id, entry);
    }

    public static Entry get(ResourceLocation id)
    {
        return NAMES.get(id);
    }

    public static Component getName(ResourceLocation id)
    {
//        var entry = NAMES.get(id);
//        if (entry == null)
//        {
//            return Text.of(id.toString());
//        }
        return Component.translatable(id.toLanguageKey("implant"));
    }

    public record Entry(Component name)
    {

    }
}
