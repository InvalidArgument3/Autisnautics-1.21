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

package com.neep.neepmeat.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.entity.smudge.SmudgeEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SmudgeEntityRenderer extends EntityRenderer<SmudgeEntity>
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(NeepMeat.NAMESPACE, "textures/entity/smudge.png");

    public SmudgeEntityRenderer(EntityRendererProvider.Context ctx)
    {
        super(ctx);
    }

    @Override
    public void render(SmudgeEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light)
    {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
//        DebugRenderUtil.renderHCircle(matrices, (float) (entity.getX() - camPos.x), (float) (entity.getY() - camPos.y), (float) (entity.getZ() - camPos.z), 1, 1, 0, 0, vertexConsumers);

        float rx = (float) (Mth.lerp(tickDelta, entity.xo, entity.getX()) - camPos.x());
        float ry = (float) (Mth.lerp(tickDelta, entity.yo, entity.getY()) - camPos.y());
        float rz = (float) (Mth.lerp(tickDelta, entity.zo, entity.getZ()) - camPos.z());

        float angle = entity.getAngle();

        Quaternionf quaternionf;
        if (angle == 0.0F)
        {
            quaternionf = camera.rotation();
        }
        else
        {
            quaternionf = new Quaternionf(camera.rotation());
            quaternionf.rotateZ(angle);
        }

        Vector3f[] vector3fs = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };

        float i = entity.getSize(tickDelta);

        Matrix3f normal = matrices.last().normal();
        Matrix4f position = matrices.last().pose();

        for (int j = 0; j < 4; j++)
        {
            Vector3f vector3f = vector3fs[j];
            vector3f.rotate(quaternionf);
            vector3f.mul(i);
            vector3f.add(rx, ry, rz);
            vector3f.mulPosition(position);
        }

        float u0 = 0;
        float u1 = 1;
        float v0 = 0;
        float v1 = 1;

        float distance = Mth.sqrt((float) camPos.distanceToSqr(entity.getX(), entity.getY(), entity.getZ()));

        float alpha = 1;
        float maxDist = 15;
        float minDist = 8;
        if (distance < maxDist)
        {
            float f = (float) Math.max(0, (distance - minDist) / (maxDist - minDist));
            alpha *= f;
        }

        int col = entity.getCol();
        int mask = ~(0xff << 24);
        col = (col & mask) | ((int) (alpha * 0xff) << 24);
//        col = 0xffffff00 & (0xaa << 16);
//        Vector4f col = new Vector4f(1, 1, 1, 1);
//        float r = col.x;
//        float g = col.y;
//        float b = col.z;
//        float a = col.w;

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.entityTranslucent(getTexture(entity)));

        // This crashes when Sodium/Indium aren't installed.
        // No idea why.
        // ZapEntityRenderer uses the same RenderLayer in the same way and is fine.
//        vertexConsumer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z())
//                .color(col)
//                .texture(u1, v1)
//                .overlay(OverlayTexture.DEFAULT_UV)
//                .light(light)
//                .normal(normal, 0, 1, 0)
//                .next();
//        vertexConsumer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z())
//                .texture(u1, v0)
//                .color(col)
//                .light(light)
//                .overlay(OverlayTexture.DEFAULT_UV)
//                .normal(normal, 0, 1, 0)
//                .next();
//        vertexConsumer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z())
//                .texture(u0, v0)
//                .color(col)
//                .light(light)
//                .overlay(OverlayTexture.DEFAULT_UV)
//                .normal(normal, 0, 1, 0)
//                .next();
//        vertexConsumer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z())
//                .texture(u0, v1)
//                .color(col)
//                .light(light)
//                .overlay(OverlayTexture.DEFAULT_UV)
//                .normal(normal, 0, 1, 0)
//                .next();
    }

    @Override
    public ResourceLocation getTexture(SmudgeEntity entity)
    {
        return TEXTURE;
    }
}
