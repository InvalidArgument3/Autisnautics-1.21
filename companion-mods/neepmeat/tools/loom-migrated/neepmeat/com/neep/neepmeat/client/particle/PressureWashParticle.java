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

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.meatlib.client.MeatlibFluidRendering;
import com.neep.neepmeat.api.block.PressureWashable;
import com.neep.neepmeat.mixin.client.ParticleAccessor;
import com.neep.neepmeat.particle.PressureWashParticleType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class PressureWashParticle extends RisingParticle
{
    private final float initialScale;
    private float targetScale;

    public PressureWashParticle(ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, float gravity, int packedCol)
    {
        super(clientWorld, d, e, f, g, h, i);
        this.gravity = gravity;

        this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
        this.initialScale = quadSize;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = roll;

        targetScale = Mth.lerp(Math.min(1f, age / 10f), 0.8f * initialScale, initialScale);
        quadSize = targetScale;

        setColour(packedCol);
    }

    public void setColour(int packed)
    {
        this.rCol = (float) (packed >> 16 & 0xFF) / 255.0f;
        this.gCol = (float) (packed >> 8 & 0xFF) / 255.0f;
        this.bCol = (float) (packed & 0xFF) / 255.0f;
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
        setAlpha(1 - age / (float) lifetime);

        Vec3 velocity = new Vec3(xd, yd, zd);
        if (velocity.length() < 0.3 || ((ParticleAccessor) this).isStopped())
            targetScale = 4 * initialScale;
        else
            targetScale = Mth.lerp(Math.min(1f, age / 10f), 0.8f * initialScale, initialScale);

        BlockPos inPos = BlockPos.containing(x, y, z);
        var inState = level.getBlockState(inPos);
        var washable = PressureWashable.get(inState);
        if (washable != null)
        {
            int col = washable.tint();
            if (col != -1)
                setColour(col);
        }
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta)
    {
        super.render(vertexConsumer, camera, tickDelta);
        quadSize = Mth.lerp(0.8f, quadSize, targetScale);
    }

    public record Factory(SpriteSet spriteProvider) implements ParticleProvider<PressureWashParticleType.Parameters>
    {
        @Override
        public Particle createParticle(PressureWashParticleType.Parameters parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
        {
            int col = parameters.col();
            if (parameters.fluid() != Fluids.EMPTY)
            {
                int fluidCol = MeatlibFluidRendering.getColor(FluidVariant.of(parameters.fluid()));
                if (col == 0xffffff)
                    col = fluidCol;
            }

            var particle = new PressureWashParticle(world, x, y, z, velocityX, velocityY, velocityZ,
                    parameters.gravity(), col
            );

            particle.pickSprite(spriteProvider);
            return particle;
        }
    }
}
