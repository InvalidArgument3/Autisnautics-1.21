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

package com.neep.meatweapons.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.client.FullBeamRenderer;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;

public class LongBulletTrailEffect extends BeamGraphicsEffect
{
    public static final ResourceLocation TRAIL_TEXTURE = ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "textures/misc/long_bullet_trail.png");
    public static final RenderType TRAIL_LAYER = RenderType.entityTranslucent(TRAIL_TEXTURE);

    private final RandomSource random = RandomSource.create();

    public LongBulletTrailEffect(Level world, Vec3 start, Vec3 end, Vec3 col, float scale, int maxTime, FriendlyByteBuf buf)
    {
        super(world, start, end, col, scale, maxTime, buf);
    }


    @Override
    public void tick()
    {
        super.tick();
        double d = 0.1f;
        world.addParticle(MWParticles.LONG_BOI_HIT, end.x, end.y, end.z,
                random.triangle(0, d),
                random.triangle(0, d),
                random.triangle(0, d));
    }

    @Override
    public void render(Camera camera, PoseStack matrices, MultiBufferSource consumers, float tickDelta)
    {
        matrices.pushPose();

        float x = Math.max(0, (maxTime - time + 2 - tickDelta) / (float) maxTime);

        // Baseline
//        VertexConsumer consumer = consumers.getBuffer(RenderType.getEntityTranslucentCull(TRAIL_TEXTURE));
//        FullBeamRenderer.renderBeamSquare(matrices, consumer, camera.getPos(),
//                start, end, 0, (int) col.x, (int) col.y, (int) col.z,
//                maxTime > 0 ? (int) (255 * x) : 255, scale, LightTexture.MAX_LIGHT_COORDINATE);

        // Using a custom core shader - will probably fail with iris
//        VertexConsumer consumer = consumers.getBuffer(MWShaders.BRIGHT_BEAM);
//        BeamRenderer.renderBeamPos(matrices, consumer, camera.getPos(),
//                start, end, 0, (int) col.x, (int) col.y, (int) col.z,
//                maxTime > 0 ? (int) (255 * x) : 255, scale);


        var camPos = camera.getPosition();
        float radius = scale;
        float offset = 1.5f + radius; // offset from origin at which the main beam effect starts
        Vec3 direction = end.subtract(start).normalize();

        Vec3 start2 = start.add(direction.scale(offset));
        Vec3 ray2 = end.subtract(start2);
        float length = (float) ray2.length();

        float pitch = (float) Math.atan2(direction.x(), direction.z());
        float yaw = (float) Math.asin(-direction.y() / direction.length());

        matrices.translate(start2.x - camPos.x, start2.y - camPos.y, start2.z - camPos.z);

        matrices.mulPose(Axis.YP.rotation(pitch));
        matrices.mulPose(Axis.XP.rotation(yaw - Mth.HALF_PI));

        matrices.pushPose();
        matrices.translate(0, 0.5f, 0);
        float s = 1 - x * x;
        matrices.scale(1 + s, 1, 1 + s);
        drawHalo(matrices, consumers, 1 - s);
        matrices.popPose();

        VertexConsumer consumer = consumers.getBuffer(RenderType.lightning());
        FullBeamRenderer.renderFullBeam(matrices, radius, length, 6, 1, 1, 1, maxTime > 0 ? x : 1, consumer);
        FullBeamRenderer.renderFullBeam(matrices, radius * 0.7f, length, 6, 1, 1, 1, 0.7f * (maxTime > 0 ? x : 1), consumer);

        matrices.popPose();
    }

    private void drawHalo(PoseStack matrices, MultiBufferSource vcp, float a)
    {
        var entry = matrices.last();
        var position = entry.pose();
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "textures/misc/halo_1.png");
        VertexConsumer consumer = vcp.getBuffer(RenderType.entityTranslucentEmissive(texture));
        Matrix3f normal = new Matrix3f();

        float d = 2;

        int o = OverlayTexture.NO_OVERLAY;
        int l = LightTexture.FULL_BRIGHT;

        consumer.addVertex(position, -d, 0, -d)
                .setColor(1.0f, 1, 1, a)
                .setUv(0, 0)
                .setOverlay(o).setLight(l).setNormal(normal, 0, 1, 0);
        consumer.addVertex(position, -d, 0, d)
                .setColor(1.0f, 1, 1, a)
                .setUv(0, 1)
                .setOverlay(o).setLight(l).setNormal(normal, 0, 1, 0);
        consumer.addVertex(position, d, 0, d)
                .setColor(1.0f, 1, 1, a)
                .setUv(1, 1)
                .setOverlay(o).setLight(l).setNormal(normal, 0, 1, 0);
        consumer.addVertex(position, d, 0, -d)
                .setColor(1.0f, 1, 1, a)
                .setUv(1, 0)
                .setOverlay(o).setLight(l).setNormal(normal, 0, 1, 0);
    }
}
