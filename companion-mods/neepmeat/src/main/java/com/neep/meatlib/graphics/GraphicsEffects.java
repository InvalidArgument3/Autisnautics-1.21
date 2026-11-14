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

package com.neep.meatlib.graphics;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.network.GlobalChannelManager;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class GraphicsEffects
{
    public static void init()
    {

    }

    public static ResourceKey<Registry<GraphicsEffectType>> GRAPHICS_EFFECTS_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MeatLib.NAMESPACE, "graphics_effects"));
    public static MappedRegistry<GraphicsEffectType> GRAPHICS_EFFECTS = FabricRegistryBuilder.createSimple(GRAPHICS_EFFECTS_KEY).buildAndRegister();

    public static final GlobalChannelManager<SyncEffect> CHANNEL = GlobalChannelManager.create(ResourceLocation.fromNamespaceAndPath(MeatLib.NAMESPACE, "sync_beam_effect"),
            SyncEffect.FORMAT);

    public static GraphicsEffectType register(String namespace, String id, GraphicsEffectType factory)
    {
        return Registry.register(GRAPHICS_EFFECTS, ResourceLocation.fromNamespaceAndPath(namespace, id), factory);
    }

}
