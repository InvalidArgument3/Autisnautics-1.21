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

package com.neep.neepmeat.api.processing;

import com.neep.meatlib.client.MeatlibItemColours;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class OreDustColours
{
    /// Can be referenced on a dedicated server, but will crash if called.
    public static int getTint(OreDustRegistry registry, ResourceLocation itemId, boolean brighten)
    {
        var entry = registry.getClient(BuiltInRegistries.ITEM.get(itemId));
        if (entry != null && entry.tint() != -1)
            return entry.tint();

        int col = MeatlibItemColours.getTint(itemId, brighten);

        return col;
    }
}
