package com.neep.meatweapons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class FullBeamRenderer
{
    public static void renderBeamSquare(PoseStack matrices, VertexConsumer vertexConsumer, Vec3 cameraPos, Vec3 p0, Vec3 p1, float rollDegrees, int r, int g, int b, int a, float t, int l)
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
        matrices.mulPose(Axis.ZP.rotationDegrees(rollDegrees + 45));

        PoseStack.Pose entry = matrices.last();
        Matrix4f model = entry.pose();

        float t1 = 1 * t;

        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, -t1 / 2, -t / 2, 0, 0, 1); // p0
        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, -t1 / 2, -t / 2, (float) beam.length(), 1, 1); // p1
        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, -t1 / 2, t / 2, (float) beam.length(), 1, 0); // p1
        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, -t1 / 2, t / 2, 0, 0, 0); // p0

        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, t / 2, t / 2, 0, 0, 0); // p0
        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, t1 / 2, t / 2, (float) beam.length(), 1, 0); // p1
        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, t1 / 2, -t / 2, (float) beam.length(), 1, 1); // p1
        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, t1 / 2, -t / 2, 0, 0, 1); // p0


        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, t / 2, -t1 / 2, 0, 0, 0); // p0
        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, t / 2, -t1 / 2, (float) beam.length(), 1, 0); // p1
        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, -t / 2, -t1 / 2, (float) beam.length(), 1, 1); // p1
        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, -t / 2, -t1 / 2, 0, 0, 1); // p0

        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, -t / 2, t1 / 2, 0, 0, 1); // p0
        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, -t / 2, t1 / 2, (float) beam.length(), 1, 1); // p1
        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, t / 2, t1 / 2, (float) beam.length(), 1, 0); // p1
        BeamRenderer.vertex(vertexConsumer, model, normal, r, g, b, a, l, t / 2, t1 / 2, 0, 0, 0); // p0

        matrices.popPose();
    }

    public static void renderFullBeam(PoseStack matrices, float radius, float length, int segments, float r, float g, float b, float a, VertexConsumer consumer)
    {
        Matrix4f top = matrices.last().pose();

        int min = (int) Math.ceil(segments / 4f);
        int lat = min * 4;
        int lon = min * 4;

        for (int i = 0; i < lon; ++i)
        {
            float yaw1 = Mth.TWO_PI * ((float) i / lon);
            float yaw2 = Mth.TWO_PI * ((float) (i + 1) / lon);

            // Only make half a wedge.
            for (int j = lat / 4; j < lat / 2; ++j)
            {
                // Subtract half pi to rotate the wedge
                float pitch1  = Mth.TWO_PI * ((float) j / lat) - Mth.HALF_PI;
                float pitch2  = Mth.TWO_PI * ((float) (j + 1) / lat) - Mth.HALF_PI;

                float xzLen1 = radius * Mth.cos(pitch1);
                float xzLen2 = radius * Mth.cos(pitch2);

                float y1 = radius * Mth.sin(pitch1);
                float y2 = radius * Mth.sin(pitch2);

                float x1 = xzLen1 * Mth.cos(yaw1);
                float x2 = xzLen1 * Mth.cos(yaw2);
                float z1 = xzLen1 * Mth.sin(yaw1);
                float z2 = xzLen1 * Mth.sin(yaw2);

                float x3 = xzLen2 * Mth.cos(yaw1);
                float x4 = xzLen2 * Mth.cos(yaw2);
                float z3 = xzLen2 * Mth.sin(yaw1);
                float z4 = xzLen2* Mth.sin(yaw2);

                consumer.vertex(top, x2, y1, z2).color(r, g, b, a).endVertex();
                consumer.vertex(top, x1, y1, z1).color(r, g, b, a).endVertex();
                consumer.vertex(top, x3, y2, z3).color(r, g, b, a).endVertex();
                consumer.vertex(top, x4, y2, z4).color(r, g, b, a).endVertex();
            }
        }

        for (int i = 0; i < lon; ++i)
        {
            float yaw1 = Mth.TWO_PI * ((float) i / lon);
            float yaw2 = Mth.TWO_PI * ((float) (i + 1) / lon);

            float y1 = -length;
            float y2 = 0;

            float x1 = radius * Mth.cos(yaw1);
            float x2 = radius * Mth.cos(yaw2);
            float z1 = radius * Mth.sin(yaw1);
            float z2 = radius * Mth.sin(yaw2);

            float x3 = radius * Mth.cos(yaw1);
            float x4 = radius * Mth.cos(yaw2);
            float z3 = radius * Mth.sin(yaw1);
            float z4 = radius * Mth.sin(yaw2);

            consumer.vertex(top, x2, y1, z2).color(r, g, b, a).endVertex();
            consumer.vertex(top, x1, y1, z1).color(r, g, b, a).endVertex();
            consumer.vertex(top, x3, y2, z3).color(r, g, b, a).endVertex();
            consumer.vertex(top, x4, y2, z4).color(r, g, b, a).endVertex();
        }
    }
}
