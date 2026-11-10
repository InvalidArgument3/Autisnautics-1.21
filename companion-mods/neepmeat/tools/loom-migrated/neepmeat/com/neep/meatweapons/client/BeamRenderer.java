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

package com.neep.meatweapons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;


@Environment(value= EnvType.CLIENT)
public class BeamRenderer
{
    public static void renderBeam(PoseStack matrices, VertexConsumer vertexConsumer, Vec3 cameraPos, Vec3 p0, Vec3 p1, int r, int g, int b, int a, float t, int l)
    {
        renderBeam(matrices, vertexConsumer, cameraPos, p0, p1, 0, r, g, b, a, t, l);
    }

    public static void renderBeam(PoseStack matrices, VertexConsumer vertexConsumer, Vec3 cameraPos, Vec3 p0, Vec3 p1, float rollDegrees, int r, int g, int b, int a, float t, int l)
    {
        matrices.pushPose();
        Vec3 beam = p1.subtract(p0);

        Vec3 offset = p0.subtract(cameraPos);
        matrices.translate(offset.x, offset.y, offset.z);

        double pitch = Math.atan2(beam.x(), beam.z());
        double yaw = Math.asin(-beam.y() / beam.length());

        matrices.mulPose(Axis.YP.rotation((float) pitch));
        matrices.mulPose(Axis.XP.rotation((float) yaw));
        Matrix3f normal = matrices.last().normal();
        matrices.mulPose(Axis.ZP.rotationDegrees(rollDegrees));

        PoseStack.Pose entry = matrices.last();
        Matrix4f model = entry.pose();

        vertex(vertexConsumer, model, normal, r, g, b, a, l, 0, -t / 2, 0, 0, 1); // p0
        vertex(vertexConsumer, model, normal, r, g, b, a, l, 0, -t / 2, (float) beam.length(), 1, 1); // p1
        vertex(vertexConsumer, model, normal, r, g, b, a, l, 0, t / 2, (float) beam.length(), 1, 0); // p1
        vertex(vertexConsumer, model, normal, r, g, b, a, l, 0, t / 2, 0, 0, 0); // p0

        vertex(vertexConsumer, model, normal, r, g, b, a, l, -t / 2, 0, 0, 0, 1); // p0
        vertex(vertexConsumer, model, normal, r, g, b, a, l, -t / 2, 0, (float) beam.length(), 1, 1); // p1
        vertex(vertexConsumer, model, normal, r, g, b, a, l, t / 2, 0, (float) beam.length(), 1, 0); // p1
        vertex(vertexConsumer, model, normal, r, g, b, a, l, t / 2, 0, 0, 0, 0); // p0

        matrices.mulPose(Axis.ZP.rotationDegrees(45));

        vertex(vertexConsumer, model, normal, r, g, b, a, l, 0, -t / 2, 0, 0, 1); // p0
        vertex(vertexConsumer, model, normal, r, g, b, a, l, 0, -t / 2, (float) beam.length(), 1, 1); // p1
        vertex(vertexConsumer, model, normal, r, g, b, a, l, 0, t / 2, (float) beam.length(), 1, 0); // p1
        vertex(vertexConsumer, model, normal, r, g, b, a, l, 0, t / 2, 0, 0, 0); // p0

        vertex(vertexConsumer, model, normal, r, g, b, a, l, -t / 2, 0, 0, 0, 1); // p0
        vertex(vertexConsumer, model, normal, r, g, b, a, l, -t / 2, 0, (float) beam.length(), 1, 1); // p1
        vertex(vertexConsumer, model, normal, r, g, b, a, l, t / 2, 0, (float) beam.length(), 1, 0); // p1
        vertex(vertexConsumer, model, normal, r, g, b, a, l, t / 2, 0, 0, 0, 0); // p0

        matrices.popPose();
    }

    /// position texture attributes
    public static void renderBeamPos(PoseStack matrices, VertexConsumer vertexConsumer, Vec3 cameraPos, Vec3 p0, Vec3 p1, float rollDegrees, int r, int g, int b, int a, float t)
    {
        matrices.pushPose();
        Vec3 beam = p1.subtract(p0);

        Vec3 offset = p0.subtract(cameraPos);
        matrices.translate(offset.x, offset.y, offset.z);

        double pitch = Math.atan2(beam.x(), beam.z());
        double yaw = Math.asin(-beam.y() / beam.length());

        matrices.mulPose(Axis.YP.rotation((float) pitch));
        matrices.mulPose(Axis.XP.rotation((float) yaw));
        Matrix3f normal = matrices.last().normal();
        matrices.mulPose(Axis.ZP.rotationDegrees(rollDegrees));

        PoseStack.Pose entry = matrices.last();
        Matrix4f model = entry.pose();

        int l = 0;
        vertexPos(vertexConsumer, model, normal, r, g, b, a, l, 0, -t / 2, 0, 0, 1); // p0
        vertexPos(vertexConsumer, model, normal, r, g, b, a, l, 0, -t / 2, (float) beam.length(), 1, 1); // p1
        vertexPos(vertexConsumer, model, normal, r, g, b, a, l, 0, t / 2, (float) beam.length(), 1, 0); // p1
        vertexPos(vertexConsumer, model, normal, r, g, b, a, l, 0, t / 2, 0, 0, 0); // p0

        vertexPos(vertexConsumer, model, normal, r, g, b, a, l, -t / 2, 0, 0, 0, 1); // p0
        vertexPos(vertexConsumer, model, normal, r, g, b, a, l, -t / 2, 0, (float) beam.length(), 1, 1); // p1
        vertexPos(vertexConsumer, model, normal, r, g, b, a, l, t / 2, 0, (float) beam.length(), 1, 0); // p1
        vertexPos(vertexConsumer, model, normal, r, g, b, a, l, t / 2, 0, 0, 0, 0); // p0

        matrices.popPose();
    }

    // Needs a loooong, vertical texture
    public static void renderBeam2(PoseStack matrices, VertexConsumer vertexConsumer, Vec3 cameraPos, Vec3 p0, Vec3 p1, float rollDegrees,
                                   int r, int g, int b, int a, float t, int l, int a1)
    {
        matrices.pushPose();
        Vec3 beam = p1.subtract(p0);

        Vec3 offset = p0.subtract(cameraPos);
        matrices.translate(offset.x, offset.y, offset.z);

        double pitch = Math.atan2(beam.x(), beam.z());
        double yaw = Math.asin(-beam.y() / beam.length());

        matrices.mulPose(Axis.YP.rotation((float) pitch));
        matrices.mulPose(Axis.XP.rotation((float) yaw));
        Matrix3f normal = matrices.last().normal();
        matrices.mulPose(Axis.ZP.rotationDegrees(rollDegrees));

        PoseStack.Pose entry = matrices.last();
        Matrix4f model = entry.pose();

        float len = (float) beam.length();

        // Vertical texture, so it's v;
        float end = len / 32f;
//        float end = 1;

        vertex(vertexConsumer, model, normal, r, g, b, a1, l, 0, -t / 2, 0, 1, 0); // p0
        vertex(vertexConsumer, model, normal, r, g, b, a, l, 0, -t / 2, len, 1, end); // p1
        vertex(vertexConsumer, model, normal, r, g, b, a, l, 0, t / 2, len, 0, end); // p1
        vertex(vertexConsumer, model, normal, r, g, b, a1, l, 0, t / 2, 0, 0, 0); // p0

        vertex(vertexConsumer, model, normal, r, g, b, a1, l, -t / 2, 0, 0, 1, 0); // p0
        vertex(vertexConsumer, model, normal, r, g, b, a, l, -t / 2, 0, len, 1, end); // p1
        vertex(vertexConsumer, model, normal, r, g, b, a, l, t / 2, 0, len, 0, end); // p1
        vertex(vertexConsumer, model, normal, r, g, b, a1, l, t / 2, 0, 0, 0, 0); // p0

        matrices.popPose();
    }

    public static void renderTube(Vec3 camPos, Vec3 p0, Vec3 p1, float tickDelta, int age, PoseStack matrices, VertexConsumer consumer, int light, float scale, int a0)
    {
        matrices.pushPose();
        Vec3 beam = p1.subtract(p0);
        float g = (float) beam.length();

        Vec3 offset = p0.subtract(camPos);
        matrices.translate(offset.x, offset.y, offset.z);

        double pitch = Math.atan2(beam.x(), beam.z());
        double yaw = Math.asin(-beam.y() / beam.length());

        matrices.mulPose(Axis.YP.rotation((float) pitch));
        matrices.mulPose(Axis.XP.rotation((float) yaw));

//        float h = 0.0F - (age + tickDelta) * 0.04F;
//        float i = g / 32.0F - (age + tickDelta) * 0.04F;
        float h = 0.0F - (age + tickDelta) * 0.04F;
        float i = g / 32.0F - (age + tickDelta) * 0.04F;
        float k = 0.0F;
        float m = 0.0F;
        PoseStack.Pose entry = matrices.last();
        Matrix4f matrix4f = entry.pose();
        Matrix3f matrix3f = entry.normal();

        float l = scale;
        for (int seg = 1; seg <= 8; ++seg)
        {
            float o = Mth.sin(seg * 6.2831855F / 8.0F) * scale;
            float p = Mth.cos(seg * 6.2831855F / 8.0F) * scale;
            float q = seg / 8.0F;
            consumer.vertex(matrix4f, k, l, 0.0F).color(255, 255, 255, a0).uv(m, h).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, k, l, g).color(255, 255, 255, 255).uv(m, i).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, o, p, g).color(255, 255, 255, 255).uv(q, i).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, o, p, 0.0F).color(255, 255, 255, a0).uv(q, h).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
//            vertex(consumer, matrix4f, matrix3f, 255, 255, 255, 255, light, new Vec3d(k*.2, l*.2, 0), 0, 1);
//            vertex(consumer, matrix4f, matrix3f, 255, 255, 255, 255, light, new Vec3d(k, l, g), 0, 1);
//            vertex(consumer, matrix4f, matrix3f, 255, 255, 255, 255, light, new Vec3d(o, p, g), 0, 1);
//            vertex(consumer, matrix4f, matrix3f, 255, 255, 255, 255, light, new Vec3d(o*.2, p*.2, 0), 0, 1);
            k = o;
            l = p;
            m = q;
        }

        matrices.popPose();
    }

    public static void vertex(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMatrix, int r, int g, int b, int a, int light, float x, float y, float z, float u, float v)
    {
        buffer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normalMatrix, 0.0f, 1.0f, 0.0f)
                .endVertex();
    }

    public static void vertexPos(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMatrix, int r, int g, int b, int a, int light, float x, float y, float z, float u, float v)
    {
        buffer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .endVertex();
    }
}
