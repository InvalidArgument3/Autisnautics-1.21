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

package com.neep.neepmeat.particle;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class FallingParticle extends TextureSheetParticle
{
    public FallingParticle(ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, float gravityStrength)
    {
        super(clientWorld, d, e, f, g, h, i);
        this.gravity = gravityStrength;
//        this.velocityMultiplier = 10;

//        this.velocityX = velocityX + (Math.random() * 2.0 - 1.0) * (double)0.4f;
//        this.velocityY = velocityY + (Math.random() * 2.0 - 1.0) * (double)0.4f;
//        this.velocityZ = velocityZ + (Math.random() * 2.0 - 1.0) * (double)0.4f;
//        double j = (Math.random() + Math.random() + 1.0) * (double)0.15f;
//        double k = Math.sqrt(this.velocityX * this.velocityX + this.velocityY * this.velocityY + this.velocityZ * this.velocityZ);
//        this.velocityX = this.velocityX / k * j * (double)0.4f;
//        this.velocityY = this.velocityY / k * j * (double)0.4f + (double)0.1f;
//        this.velocityZ = this.velocityZ / k * j * (double)0.4f;
        this.xd = g;
        this.yd = h;
        this.zd = i;
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider)
        {
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
        {
            var particle = new FallingParticle(world, x, y, z, velocityX, velocityY, velocityZ, 1);
            particle.pickSprite(spriteProvider);
            return particle;
        }
    }
}
