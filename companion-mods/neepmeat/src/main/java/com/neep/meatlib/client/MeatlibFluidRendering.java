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

import com.mojang.blaze3d.platform.NativeImage;
import com.neep.meatlib.mixin.client.SpriteContentsAccessor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import java.util.HashMap;
import java.util.Map;

/// For getting more accurate fluid colour information based on sprite data as well as tint.
/// If fluid sprites change in a resource reload, this will keep the old values. Oh well.
@OnlyIn(Dist.CLIENT)
public class MeatlibFluidRendering
{
    private MeatlibFluidRendering() {}

    private static final Map<FluidVariant, Integer> MAP = new HashMap<>();

    public static int getColor(FluidVariant fluid)
    {
        return MAP.computeIfAbsent(fluid, MeatlibFluidRendering::createTint);
//        return calculateAverage(FluidVariantRendering.getSprite(fluid), FluidVariantRendering.getColor(fluid));
    }

    public static int getColor(FluidVariant fluid, boolean brighten)
    {
        int col = getColor(fluid);

        if (brighten)
        {
            col = brighten(col, 1.5f);
        }

        return col;
    }

    public static int brighten(int col, float fac)
    {
        float a = (float) (col >> 24 & 0xFF);

        float r = (float) (col >> 16 & 0xFF) * fac;
        float g = (float) (col >> 8 & 0xFF) * fac;
        float b = (float) (col & 0xFF) * fac;

        // Find the maximum channel and normalise to ensure the hue is retained if a channel exceeds 255
        float max = Math.max(r, Math.max(b, a));

        r = r / max * 255f;
        g = g / max * 255f;
        b = b / max * 255f;

        return pack((int) r, (int) g, (int) b, (int) a);
    }

    /// Calculates the true tint based on the fluid sprite and the fluid's registered tint
    private static int createTint(FluidVariant fluid)
    {
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);

        if (sprite != null)
        {
            return calculateAverage(sprite, FluidVariantRendering.getColor(fluid));
        }

        return 0xffffff;
    }

    public static int pack(int r, int g, int b, int a)
    {
        return (a << 24)
                | ((r & 0xFF) << 16)
                | ((g & 0xFF) << 8)
                | (b & 0xFF);
    }

    public static int calculateAverage(TextureAtlasSprite sprite, int tint)
    {
        NativeImage image = ((SpriteContentsAccessor) sprite.contents()).getImage();

        float red = 1;
        float green = 1;
        float blue = 1;
        if (tint != -1)
        {
            red = (float) (tint >> 16 & 0xFF) / 255.0f;
            green = (float) (tint >> 8 & 0xFF) / 255.0f;
            blue = (float) (tint & 0xFF) / 255.0f;
        }

        int pixels = 0;
        int cumR = 0;
        int cumG = 0;
        int cumB = 0;

        for (int i = 0; i < image.getWidth(); ++i)
        {
            for (int j = 0; j < image.getHeight(); ++j)
            {
                int agbr = image.getPixelRGBA(i, j);
                int alpha = agbr >> 24;
                if (alpha != 0)
                {
                    cumR += agbr & 0xff;
                    cumG += (agbr >> 8) & 0xff;
                    cumB += (agbr >> 16) & 0xff;

                    pixels++;
                }
            }
        }

        if (pixels == 0)
            return -1;

        cumR = (int) (cumR / (float) pixels * red);
        cumG = (int) (cumG / (float) pixels * green);
        cumB = (int) (cumB / (float) pixels * blue);

        return pack(cumR, cumG, cumB, 0xff);
    }
}
