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

import com.neep.neepmeat.init.NMFluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.Fluid;

@OnlyIn(Dist.CLIENT)
public class BloodDripParticleFactory implements ParticleProvider<SimpleParticleType>
{
    protected final SpriteSet spriteProvider;
    private final RandomSource random;

    public BloodDripParticleFactory(SpriteSet spriteProvider)
    {
        this.spriteProvider = spriteProvider;
        this.random = RandomSource.create();
    }

    public Particle createParticle(SimpleParticleType defaultParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i)
    {
        BloodDripParticle drip = new BloodDripParticle(clientWorld, d, e, f, com.neep.neepmeat.fluid.FluidForms.still(NMFluids.BLOOD));
        drip.pickSprite(this.spriteProvider);
        return drip;
    }

    @OnlyIn(Dist.CLIENT)
    private static class BloodDripParticle extends DripParticle
    {
//        private final ParticleEffect nextParticle;
        private int state = 0;

        public BloodDripParticle(ClientLevel world, double x, double y, double z, Fluid fluid)
        {
            super(world, x, y, z, fluid);
            this.gravity *= 0.02F;
            this.lifetime = 40;
        }

        protected void preMoveUpdate()
        {
            if (this.lifetime-- <= 0)
            {
                state++;
                this.lifetime = 50;
            }
            if (state >= 2)
            {
                this.remove();
            }
        }

        protected void postMoveUpdate()
        {
            if (state == 0)
            {
                this.xd *= 0.02;
                this.yd *= 0.02;
                this.zd *= 0.02;
            }
            else
            {
                this.friction = 1;
                this.yd = -0.5;
            }


            if (this.onGround)
                remove();

        }
    }
}
