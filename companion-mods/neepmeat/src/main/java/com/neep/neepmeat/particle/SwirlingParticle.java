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
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Random;

public class SwirlingParticle extends TextureSheetParticle
{
    private final BlockPos blockPos;
    private final float sampleU;
    private final float sampleV;
    private final double radius;
    private final double originX;
    private final double originY;
    private final double originZ;
    private final double speed;
    private double angle;

    public SwirlingParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, double radius, double angle, double speed, BlockState state)
    {
//        this(world, x, y, z, velocityX, velocityY, velocityZ, radius, angle, speed, state, BlockPos.ofFloored(x, y, z));
        this(world, x, y, z, velocityX, velocityY, velocityZ, radius, angle, speed, state, BlockPos.containing(x, y, z));
    }

    public SwirlingParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, double radius, double angle, double speed, BlockState state, BlockPos blockPos)
    {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.radius = radius;
        this.speed = speed;
        this.angle = random.nextDouble() * 2 * Math.PI;
        this.originX = x;
        this.originY = y;
        this.originZ = z;

        this.x = getRectX(originX, this.angle, radius);
        this.z = getRectZ(originZ, this.angle, radius);

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.blockPos = blockPos;
        this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state));
        this.gravity = 1.0f;
        this.rCol = 0.6f;
        this.gCol = 0.6f;
        this.bCol = 0.6f;
        if (!state.is(Blocks.GRASS_BLOCK)) {
            int i = Minecraft.getInstance().getBlockColors().getColor(state, world, blockPos, 0);
            this.rCol *= (float)(i >> 16 & 0xFF) / 255.0f;
            this.gCol *= (float)(i >> 8 & 0xFF) / 255.0f;
            this.bCol *= (float)(i & 0xFF) / 255.0f;
        }
        this.quadSize /= 2.0f;
        this.sampleU = this.random.nextFloat() * 3.0f;
        this.sampleV = this.random.nextFloat() * 3.0f;
    }

    public static double getRectX(double x, double theta, double r)
    {
        return x + Math.cos(theta) * r;
    }

    public static double getRectZ(double z, double theta, double r)
    {
        return z + Math.sin(theta) * r;
    }

    @Override
    public void tick()
    {
        this.angle += speed;

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime)
        {
            this.remove();
            return;
        }
//        this.velocityY -= 0.04 * (double)this.gravityStrength;
//        this.move(this.velocityX, this.velocityY, this.velocityZ);

        this.x = getRectX(originX, angle, radius);
        this.z = getRectZ(originZ, angle, radius);

//        if (dx != 0.0 || dy != 0.0 || dz != 0.0)
//        {
//            this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
//            this.repositionFromBoundingBox();
//        }


        if (this.onGround && this.y == this.yo)
        {
            this.xd *= 1.1;
            this.zd *= 1.1;
        }
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        if (this.onGround) {
            this.xd *= 0.7f;
            this.zd *= 0.7f;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

//    @Override
//    protected float method_18133() {
//        return this.sprite.getFrameU((this.sampleU + 1.0f) / 4.0f * 16.0f);
//    }
//
//    @Override
//    protected float method_18134() {
//        return this.sprite.getFrameU(this.sampleU / 4.0f * 16.0f);
//    }
//
//    @Override
//    protected float method_18135() {
//        return this.sprite.getFrameV(this.sampleV / 4.0f * 16.0f);
//    }
//
//    @Override
//    protected float method_18136() {
//        return this.sprite.getFrameV((this.sampleV + 1.0f) / 4.0f * 16.0f);
//    }

    @Override
    public int getLightColor(float tint)
    {
        int i = super.getLightColor(tint);
        if (i == 0 && this.level.hasChunkAt(this.blockPos))
        {
            return LevelRenderer.getLightColor(this.level, this.blockPos);
        }
        return i;
    }

    @Environment(value=Dist.CLIENT)
    public static class Factory implements ParticleProvider<SwirlingParticleEffect>
    {
        @Override
        public Particle createParticle(SwirlingParticleEffect effect, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i)
        {
            BlockState blockState = effect.getBlockState();
            if (blockState.isAir() || blockState.is(Blocks.MOVING_PISTON))
            {
                return null;
            }
            Random rand = new Random(0);
            return new SwirlingParticle(clientWorld, d, e, f, g, h, i, effect.radius, 0, effect.speed, blockState);
        }
    }
}
