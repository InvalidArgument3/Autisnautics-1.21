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

package com.neep.neepmeat.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.world.item.ItemStack;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class ColouredItemStackParticleFactory implements ParticleProvider<ItemParticleOption>
{
    private final Function<ItemStack, Integer> getCol;

    public ColouredItemStackParticleFactory(Function<ItemStack, Integer> getCol)
    {
        this.getCol = getCol;
    }

    public Particle createParticle(
            ItemParticleOption itemStackParticleEffect, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i
    )
    {
        ItemStack stack = itemStackParticleEffect.getItem();
        var particle = new BreakingItemParticle(clientWorld, d, e, f, g, h, i, stack);

        int col = getCol.apply(stack);
        float red = (float) (col >> 16 & 0xFF) / 255.0f;
        float green = (float) (col >> 8 & 0xFF) / 255.0f;
        float blue = (float) (col & 0xFF) / 255.0f;

        particle.setColor(red, green, blue);

        return particle;
    }
}
