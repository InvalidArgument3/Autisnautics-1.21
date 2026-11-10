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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.meatweapons.client.renderer.meatgun.MeatgunParticleManager;
import com.neep.meatweapons.particle.MuzzleFlashParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class MuzzleFlashParticleFactory implements ParticleProvider<MuzzleFlashParticleType.MuzzleFlashParticleEffect>
{
    protected final SpriteSet spriteProvider;

    public MuzzleFlashParticleFactory(SpriteSet spriteProvider)
    {
        this.spriteProvider = spriteProvider;
    }

    @Nullable
    @Override
    public Particle createParticle(MuzzleFlashParticleType.MuzzleFlashParticleEffect parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
    {
        Player player = world.getPlayerByUUID(parameters.getPlayerUUID());
        MuzzleFlashParticle particle = new MuzzleFlashParticle(world, player, x, y, z, parameters.dx, parameters.dy, parameters.dz, parameters.scale, parameters.maxAge, parameters.hand);
        particle.pickSprite(spriteProvider);
        particle.setAlpha(0.8f);
        return particle;
    }

    public static class MuzzleFlashParticle extends TextureSheetParticle implements MeatgunParticle
    {
        protected final float scale0;
        protected final double x0, y0, z0;
        protected final Player player;
        private final InteractionHand hand;
        protected final float fpScale; // First person scale

        protected MuzzleFlashParticle(ClientLevel clientWorld, @Nullable Player player, double d, double e, double f, double dx, double dy, double dz, float fpScale, int maxAge, InteractionHand hand)
        {
            super(clientWorld, d, e, f);
            this.player = player;
            this.hand = hand;
            this.lifetime = maxAge;
            this.quadSize = 0.2F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
            this.scale0 = quadSize;
            this.fpScale = fpScale;

            this.x0 = dx;
            this.y0 = dy;
            this.z0 = dz;

            if (player == Minecraft.getInstance().player)
                MeatgunParticleManager.add(this);
        }

        @Override
        public void tick()
        {
            super.tick();

        }

        @Override
        public void setAlpha(float alpha)
        {
            super.setAlpha(alpha);
        }

        @Override
        public float getQuadSize(float tickDelta)
        {
            return Mth.lerp((age + tickDelta) / lifetime, scale0, scale0 * 0.8f);
        }

        @Override
        public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta)
        {
            if (player != null)
            {
                if (player == Minecraft.getInstance().player)
                {
                    return;
//                    this.x = camera.getPos().x + x0;
//                    this.y = camera.getPos().y + y0;
//                    this.z = camera.getPos().z + z0;
                }
                else
                {
                    this.x = player.getEyePosition().x + x0;
                    this.y = player.getEyePosition().y + y0;
                    this.z = player.getEyePosition().z + z0;
                }
                this.xo = x;
                this.yo = y;
                this.zo = z;
            }

            super.render(vertexConsumer, camera, tickDelta);
        }

        @Override
        public ParticleRenderType getRenderType()
        {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        @Override
        public boolean isParticleRemoved()
        {
            return !isAlive();
        }

        @Override
        public void render(PoseStack matrices, Camera camera, VertexConsumer consumer, int overlay, float tickDelta)
        {
            float f = (float) x0;
            float g = (float) y0;
            float h = (float) z0;

            float i = this.getQuadSize(tickDelta) * fpScale;

            // Gradually move the particle over its lifetime to prevent axis fighting when many are spawned in quick succession
            float offset = (age + tickDelta) / lifetime * 0.04f;
            Vector3f[] vector3fs = new Vector3f[]{
                    new Vector3f(-1.0F, -1.0F, 0F),
                    new Vector3f(-1.0F, 1.0F, 0F),
                    new Vector3f(1.0F, 1.0F, 0F),
                    new Vector3f(1.0F, -1.0F, 0F)};

            for (int j = 0; j < 4; ++j)
            {
                Vector3f vector3f = vector3fs[j];
                vector3f.mul(i);
                vector3f.add(0, 0, -offset);
                vector3f.add(f, g, h);

                var v4 = new Vector4f(vector3f, 1);
                Matrix4f mat = new Matrix4f(matrices.last().pose());
                v4.mul(mat);
                vector3f.set(v4.x, v4.y, v4.z);
            }

            float k = this.getU0();
            float l = this.getU1();
            float m = this.getV0();
            float n = this.getV1();
            int o = this.getLightColor(tickDelta);

            // Vertices have to be in this order, which is different to the order in SpriteBillboardParticle. Don't ask me why.
            consumer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z())
                    .color(this.rCol, this.gCol, this.bCol, this.alpha)
                    .uv(k, m)
                    .overlayCoords(overlay)
                    .uv2(o)
                    .normal(0, 1, 0)
                    .endVertex();
            consumer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z())
                    .color(this.rCol, this.gCol, this.bCol, this.alpha)
                    .uv(l, m)
                    .overlayCoords(overlay)
                    .uv2(o)
                    .normal(0, 1, 0)
                    .endVertex();
            consumer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z())
                    .color(this.rCol, this.gCol, this.bCol, this.alpha)
                    .uv(l, n)
                    .overlayCoords(overlay)
                    .uv2(o)
                    .normal(0, 1, 0)
                    .endVertex();
            consumer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z())
                    .color(this.rCol, this.gCol, this.bCol, this.alpha)
                    .uv(k, n)
                    .overlayCoords(overlay)
                    .uv2(o)
                    .normal(0, 1, 0)
                    .endVertex();

        }

        @Override
        public InteractionHand hand()
        {
            return hand;
        }
    }
}
