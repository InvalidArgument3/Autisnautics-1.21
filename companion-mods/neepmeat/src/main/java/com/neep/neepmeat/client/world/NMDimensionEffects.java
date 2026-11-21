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

package com.neep.neepmeat.client.world;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;

public class NMDimensionEffects
{
    public static final ResourceLocation DUAT_ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "duat");
    public static final DimensionSpecialEffects DUAT = new DuatDimensionEffects();

    public static void init()
    {
        register(DUAT_ID, DUAT);
    }

    public static void onRegister(net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent event)
    {
        event.register(DUAT_ID, DUAT);
    }

    public static DimensionSpecialEffects register(ResourceLocation id, DimensionSpecialEffects effects)
    {
        return effects;
    }
}
