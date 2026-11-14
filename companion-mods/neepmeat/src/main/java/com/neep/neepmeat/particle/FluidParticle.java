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

import com.neep.meatlib.client.fluid.FluidClient.FluidRenderHandlerRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.material.FluidState;

public class FluidParticle extends TextureSheetParticle
{
    private final float sampleU;
    private final float sampleV;

    protected FluidParticle(ClientLevel clientWorld, double d, double e, double f, double velocityX, double velocityY, double velocityZ, FluidState state)
    {
        super(clientWorld, d, e, f, velocityX, velocityY, velocityZ);
        this.x = d;
        this.y = e;
        this.z = f;
        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;

        gravity = 0.3f;

        BlockPos pos = BlockPos.containing(x, y, z);
        var renderHandler = FluidRenderHandlerRegistry.INSTANCE.get(state.getType());
        var sprite = renderHandler.getFluidSprites(level, pos, state);

        this.setSprite(sprite[0]);

//        int i = MinecraftClient.getInstance().getBlockColors().getColor(state, world, BlockPos.ofFloored(x, y, z), 0);
        int i = renderHandler.getFluidColor(clientWorld, pos, state);

        this.quadSize /= 2.0F;
        this.sampleU = this.random.nextFloat() * 3.0F;
        this.sampleV = this.random.nextFloat() * 3.0F;
    }

    public void setColour(int packed)
    {
        this.rCol *= (float) (packed >> 16 & 0xFF) / 255.0f;
        this.gCol *= (float) (packed >> 8 & 0xFF) / 255.0f;
        this.bCol *= (float) (packed & 0xFF) / 255.0f;
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    protected float getU0()
    {
        return this.sprite.getU((this.sampleU + 1.0F) / 4.0F * 16.0F);
    }

    @Override
    protected float getU1()
    {
        return this.sprite.getU(this.sampleU / 4.0F * 16.0F);
    }

    @Override
    protected float getV0()
    {
        return this.sprite.getV(this.sampleV / 4.0F * 16.0F);
    }

    @Override
    protected float getV1()
    {
        return this.sprite.getV((this.sampleV + 1.0F) / 4.0F * 16.0F);
    }

    public record Factory(SpriteSet spriteProvider) implements ParticleProvider<BlockParticleOption>
    {
        @Override
        public Particle createParticle(BlockParticleOption parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
        {
            if (parameters.getState().getFluidState().isEmpty())
                return null;

            return new FluidParticle(world, x, y, z, velocityX, velocityY, velocityZ, parameters.getState().getFluidState());
        }
    }
}
