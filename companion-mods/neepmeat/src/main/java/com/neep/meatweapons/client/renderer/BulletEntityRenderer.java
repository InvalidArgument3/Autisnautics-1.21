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

package com.neep.meatweapons.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.entity.BulletEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BulletEntityRenderer extends ArrowRenderer<BulletEntity>
{
    public BulletEntityRenderer(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(BulletEntity entity)
    {
        return ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "textures/entity/bullet.png");
    }

    @Override
    public void render(BulletEntity entity, float f, float tickDelta, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i)
    {
        // Become visible 2 ticks in
        matrixStack.pushPose();
        matrixStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(tickDelta, entity.yRotO, entity.getYRot()) - 90.0F));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(tickDelta, entity.xRotO, entity.getXRot())));
        float s = (float) entity.shakeTime - tickDelta;
        if (s > 0.0F)
        {
            float t = -Mth.sin(s * 3.0F) * s;
            matrixStack.mulPose(Axis.ZP.rotationDegrees(t));
        }

        matrixStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        float scale = 1 / 16f;
        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(0F, 0.0F, 0.0F);

        var texture = getTexture(entity);

        double ex = Mth.lerp(tickDelta, entity.xOld, entity.getX());
        double ey = Mth.lerp(tickDelta, entity.yOld, entity.getY());
        double ez = Mth.lerp(tickDelta, entity.zOld, entity.getZ());
        Minecraft client = Minecraft.getInstance();
        float dist = (float) Math.sqrt(client.gameRenderer.getMainCamera().getPosition().distanceToSqr(ex, ey, ez));
//        float alpha = (float) Mth.clamp(((double) entity.age + tickDelta) / 2f, 0., 1.);
        float alpha = Mth.clamp(Mth.lerp((dist - 1) / 1, 0f, 1f), 0, 1);

        if (alpha > 0.1f)
        {
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entityTranslucent(texture));

            PoseStack.Pose entry = matrixStack.last();
            Matrix4f matrix4f = entry.pose();
            Matrix3f matrix3f = entry.normal();

            float d = 1;
            float u0 = 0;
            float u1 = 2 / 16f;
            float v0 = 0;
            float v1 = 2 / 16f;

            int col = entity.getCol();
            float r = ((col >> 16) & 0xff) / 255f;
            float g = ((col >> 8) & 0xff) / 255f;
            float b = ((col) & 0xff) / 255f;

            // Along x
            this.vertex1(matrix4f, matrix3f, vertexConsumer, -d, -d, -d, u0, v0, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, -d, -d, d, u0, v1, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, -d, d, d, u1, v1, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, -d, d, -d, u1, v0, r, g, b, alpha, -1, 0, 0, i);

            this.vertex1(matrix4f, matrix3f, vertexConsumer, d, -d, -d, u0, v0, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, d, -d, d, u0, v1, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, d, d, d, u1, v1, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, d, d, -d, u1, v0, r, g, b, alpha, -1, 0, 0, i);

            // Along z
            this.vertex1(matrix4f, matrix3f, vertexConsumer, -d, -d, -d, u0, v0, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, -d, d, -d, u0, v1, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, d, d, -d, u1, v1, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, d, -d, -d, u1, v0, r, g, b, alpha, -1, 0, 0, i);

            this.vertex1(matrix4f, matrix3f, vertexConsumer, -d, -d, d, u0, v0, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, -d, d, d, u0, v1, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, d, d, d, u1, v1, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, d, -d, d, u1, v0, r, g, b, alpha, -1, 0, 0, i);

            // Along y
            this.vertex1(matrix4f, matrix3f, vertexConsumer, -d, -d, -d, u0, v0, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, -d, -d, d, u0, v1, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, d, -d, d, u1, v1, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, d, -d, -d, u1, v0, r, g, b, alpha, -1, 0, 0, i);

            this.vertex1(matrix4f, matrix3f, vertexConsumer, -d, d, -d,u0, v0, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, -d, d, d,u0, v1, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, d, d, d,u1, v1, r, g, b, alpha, -1, 0, 0, i);
            this.vertex1(matrix4f, matrix3f, vertexConsumer, d, d, -d,u1, v0, r, g, b, alpha, -1, 0, 0, i);


            // Fins
//            for (int u = 0; u < 4; u++)
//            {
//                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
//                this.vertex1(matrix4f, matrix3f, vertexConsumer, -8, -2, 0, 0.0F, 0, alpha, 0, 1, 0, i);
//                this.vertex1(matrix4f, matrix3f, vertexConsumer, 8, -2, 0, 1, 0.0F, alpha, 0, 1, 0, i);
//                this.vertex1(matrix4f, matrix3f, vertexConsumer, 8, 2, 0, 1, 1, alpha, 0, 1, 0, i);
//                this.vertex1(matrix4f, matrix3f, vertexConsumer, -8, 2, 0, 0.0F, 1, alpha, 0, 1, 0, i);
//            }
        }

        matrixStack.popPose();
    }

    public void vertex1(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertexConsumer, float x, float y, float z, float u, float v, float r, float g, float b, float alpha, int normalX, int normalZ, int normalY, int light)
    {
        vertexConsumer.addVertex(positionMatrix, x, y, z)
                .setColor(r, g, b, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal((float) normalX, (float) normalY, (float) normalZ)
                ;
    }

    @Override
    public boolean shouldRender(BulletEntity entity, Frustum frustum, double x, double y, double z)
    {
        return super.shouldRender(entity, frustum, x, y, z);
    }
}
