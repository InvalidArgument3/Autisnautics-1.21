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

import com.neep.meatweapons.particle.MuzzleFlashParticleType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ExpandingMuzzleFlashParticleFactory implements ParticleProvider<MuzzleFlashParticleType.MuzzleFlashParticleEffect>
{
    protected final SpriteSet spriteProvider;
    private final RandomSource random;

    public ExpandingMuzzleFlashParticleFactory(SpriteSet spriteProvider)
    {
        this.spriteProvider = spriteProvider;
        this.random = RandomSource.create();
    }

    @Nullable
    @Override
    public Particle createParticle(MuzzleFlashParticleType.MuzzleFlashParticleEffect parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
    {
        Player player = world.getPlayerByUUID(parameters.getPlayerUUID());
        ExpandingMuzzleFlashParticle particle = new ExpandingMuzzleFlashParticle(world, player, x, y, z, parameters.dx, parameters.dy, parameters.dz, parameters.scale, parameters.maxAge, parameters.hand);
        particle.pickSprite(spriteProvider);
        return particle;
    }

    public static class ExpandingMuzzleFlashParticle extends MuzzleFlashParticleFactory.MuzzleFlashParticle
    {
        protected ExpandingMuzzleFlashParticle(ClientLevel clientWorld, @Nullable Player player, double d, double e, double f, double dx, double dy, double dz, float fpScale, int maxAge, InteractionHand hand)
        {
            super(clientWorld, player, d, e, f, dx, dy, dz, fpScale, maxAge, hand);
        }

        @Override
        public float getQuadSize(float tickDelta)
        {
            return Mth.lerp((age + tickDelta) / lifetime, scale0, scale0 * 1.3f);
        }
    }
}
