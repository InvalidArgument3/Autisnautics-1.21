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

package com.neep.meatweapons.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.neepmeat.machine.live_machine.process.PhageRayProcess;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class PhageRaySplodgeParticle extends TextureSheetParticle
{
    // Keep a reference to the sprite provider here so that we can use it in another thing
    @Nullable public static Factory FACTORY;

    private final SpriteSet spriteProvider;
    private final PhageRayProcess.PhageRay parent;

    protected PhageRaySplodgeParticle(ClientLevel world, double x, double y, double z, SpriteSet spriteProvider, PhageRayProcess.PhageRay parent)
    {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.parent = parent;
        this.quadSize = 1;
    }

    public void setPrevPos(double x, double y, double z)
    {
        xo = x;
        yo = y;
        zo = z;
    }

    @Override
    protected int getLightColor(float tint)
    {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick()
    {
        super.tick();
//        Vec3d pos = parent.getClientBeamEnd(parent.getBeamOrigin(), 1);
//        setPos(pos.x, pos.y, pos.z);

        sprite = spriteProvider.get(age % 8, 8);
    }

    @Override
    public int getLifetime()
    {
        return 60;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta)
    {
        super.render(vertexConsumer, camera, tickDelta);
    }

    public static class Factory implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet provider;

        public Factory(SpriteSet provider)
        {
            this.provider = provider;
        }

        // Oh, the shame. This is utterly shameful.
        public static Factory horribleJank(SpriteSet spriteProvider)
        {
            FACTORY = new Factory(spriteProvider);
            return FACTORY;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
        {
            var particle = new PhageRaySplodgeParticle(world, x, y, z, provider, PhageRayProcess.PhageRay.EMPTY);
            particle.pickSprite(provider);
            return particle;
        }

        public PhageRaySplodgeParticle createParticle(ClientLevel world, PhageRayProcess.PhageRay phageRay)
        {
            var particle = new PhageRaySplodgeParticle(world, 0, 0, 0, provider, phageRay);
            particle.pickSprite(provider);
            return particle;
        }
    }
}
