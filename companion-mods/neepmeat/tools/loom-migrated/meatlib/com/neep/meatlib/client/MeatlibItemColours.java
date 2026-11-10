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

package com.neep.meatlib.client;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import static com.neep.meatlib.client.MeatlibFluidRendering.brighten;

public class MeatlibItemColours
{
    private static final Object2IntMap<ResourceLocation> MAP = new Object2IntOpenHashMap<>();
    private static final Object2IntMap<ResourceLocation> MAP_BRIGHT = new Object2IntOpenHashMap<>();

    private MeatlibItemColours() {}

    public static int getTint(ResourceLocation itemId, boolean brighten)
    {
        if (!brighten)
            return MAP.computeIfAbsent(itemId, Client::createTint);
        else
            return brighten(MAP.computeIfAbsent(itemId, Client::createTint), 1.5f);
    }

    @Environment(EnvType.CLIENT)
    private static class Client
    {
        /**
         * @param entryId Item I
         * @return The average colour of non-transparent pixels. -1 if invalid.
         */
        private static int createTint(ResourceLocation entryId)
        {
            Item item = BuiltInRegistries.ITEM.get(entryId);
            if (item != null)
            {
                BakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item);
                if (model != null)
                {
                    TextureAtlasSprite sprite = model.getParticleIcon();

                    if (sprite != null)
                    {
                        return MeatlibFluidRendering.calculateAverage(sprite, 0xffffff);
                    }
                }
            }
            return -1;
        }
    }
}
