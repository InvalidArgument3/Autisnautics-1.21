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

import com.neep.meatlib.transfer.FluidVariantAttributes;
import com.neep.neepmeat.particle.PressureWashParticleType;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidDripParticle extends DripParticle
{
    public FluidDripParticle(ClientLevel world, double x, double y, double z, int col, Fluid fluid)
    {
        super(world, x, y, z, fluid);
        setColour(col);
    }

    public void setColour(int packed)
    {
        this.rCol = (float) (packed >> 16 & 0xFF) / 255.0f;
        this.gCol = (float) (packed >> 8 & 0xFF) / 255.0f;
        this.bCol = (float) (packed & 0xFF) / 255.0f;
    }

    public record Factory(SpriteSet spriteProvider) implements ParticleProvider<PressureWashParticleType.Parameters>
    {
        @Override
        public Particle createParticle(PressureWashParticleType.Parameters parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
        {
            int col = parameters.col();
            if (parameters.fluid() != Fluids.EMPTY)
            {
                int fluidCol = FluidVariantAttributes.getColor(FluidVariant.of(parameters.fluid()));
                if (col == 0xffffff)
                    col = fluidCol;
            }

            var particle = new FluidDripParticle(world, x, y, z, col, parameters.fluid());

            particle.pickSprite(spriteProvider);
            return particle;
        }
    }
}
