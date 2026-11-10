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

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SoundRegistry
{
    protected static final Map<ResourceLocation, SoundEvent> SOUNDS = new LinkedHashMap<>();

    public static SoundEvent registerSound(String namespace, String path)
    {
        ResourceLocation id = new ResourceLocation(namespace, path);
        SoundEvent event = SoundEvent.createVariableRangeEvent(id);
        SOUNDS.put(id, event);
        return event;
    }

    public static synchronized void flush()
    {
        for (Map.Entry<ResourceLocation, SoundEvent> entry : SOUNDS.entrySet())
        {
            Registry.register(BuiltInRegistries.SOUND_EVENT, entry.getKey(), entry.getValue());
        }
        SOUNDS.clear();
    }
}
