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

package com.neep.neepmeat.client.fluid;

import com.neep.neepmeat.api.processing.OreFatColours;
import com.neep.neepmeat.api.processing.OreFatRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
@OnlyIn(Dist.CLIENT)
public class OreFatFluidVariantRenderHandler implements FluidVariantRenderHandler
{
    @Override
    public int getColor(FluidVariant fluidVariant, @Nullable BlockAndTintGetter view, @Nullable BlockPos pos)
    {
        CompoundTag nbt = fluidVariant.getNbt();
        if (nbt != null)
        {
            return getTint(nbt);
        }
        return -1;
    }

    private int getTint(CompoundTag nbt)
    {
        return OreFatColours.getTint(OreFatRegistry.getItemId(nbt));
    }

    @Override
    @Nullable
    public TextureAtlasSprite[] getSprites(FluidVariant fluidVariant)
    {
        // Use the fluid render handler by default.
        FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluidVariant.getFluid());

        if (fluidRenderHandler != null)
        {
            return fluidRenderHandler.getFluidSprites(null, null, fluidVariant.getFluid().defaultFluidState());
        }
        else
        {
            return null;
        }
    }
}
