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
import com.mojang.math.Axis;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.model.entity.HoundEntityModel;
import com.neep.neepmeat.entity.hound.HoundEntity;
import com.neep.neepmeat.util.SightUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public class HoundEntityRenderer extends LivingEntityRenderer<HoundEntity, HoundEntityModel>
{
//    private static final Identifier TEXTURE = new Identifier(NeepMeat.NAMESPACE, "textures/entity/hound/main.png");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/entity/follower/main.png");

    public static final ModelLayerLocation HOUND_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "hound"), "hound");

    public HoundEntityRenderer(EntityRendererProvider.Context ctx)
    {
        super(ctx, new HoundEntityModel(ctx.bakeLayer(HOUND_LAYER)), 0.5f);
    }

    @Override
    public void render(HoundEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vcp, int light)
    {
        this.shadowStrength = 0;
        Minecraft client = Minecraft.getInstance();

//        var camera = client.gameRenderer.getCamera().getPos();
//        float frame = client.getLastFrameDuration();
//        double d = MathHelper.clamp(2 * frame, 0, 1);
//        entity.smoothRenderX = MathHelper.lerp(d, entity.smoothRenderX, entity.getX());
//        entity.smoothRenderY = MathHelper.lerp(d, entity.smoothRenderY, entity.getY());
//        entity.smoothRenderZ = MathHelper.lerp(d, entity.smoothRenderZ, entity.getZ());
//        matrices.pop();
//        matrices.push();
//        matrices.translate(entity.smoothRenderX - camera.x, entity.smoothRenderY - camera.y, entity.smoothRenderZ - camera.z);

        Player player = Minecraft.getInstance().player;
        Vec3 entityPos = entity.getRopeHoldPosition(tickDelta);
        Vec3 vector = entityPos.subtract(client.gameRenderer.getMainCamera().getPosition()).normalize();
        float dot = Mth.clamp((float) ((vector.dot(player.getViewVector(tickDelta)))), 0, 1);
        dot = (float) Math.pow(2, 20 * dot - 20);

        if (SightUtil.canPlayerSee(player))
            dot = 1;

        if (dot > 0.05)
        {
            VertexConsumer consumer = vcp.getBuffer(RenderType.entityTranslucent(TEXTURE));
            float offset = (float) (entity.getPosition(tickDelta).distanceTo(player.getPosition(tickDelta)) / 16.0f);
            int num = 10;
            for (int i = 0; i < num; ++i)
            {
                matrices.pushPose();
                float frac = ((i / (float) num) + offset) % (float) num;
                float x = Mth.sin(frac * Mth.PI * 2);
                float z = Mth.cos(frac * Mth.PI * 2);
                float y = Mth.sin(frac * Mth.PI * 8);
                matrices.translate(x, y, z);
                matrices.mulPose(new Quaternionf(client.gameRenderer.getMainCamera().rotation()));
                matrices.mulPose(Axis.ZN.rotation(frac * Mth.PI));
                matrices.mulPose(Axis.YN.rotation((float) ((1 - frac) * Mth.PI +
                                        2 * Mth.clamp(Math.tan(frac + 2), -1, 1))));
                HoundEntityRenderer.glonkyQuad(consumer, matrices.last(), i, 0.1f, 1, 1, 1, dot, light);
                matrices.translate(0, 1, 0);
                HoundEntityRenderer.glonkyQuad(consumer, matrices.last(), i, 0.9f, 1, 1, 1, dot, light);
                matrices.popPose();
            }
        }
    }

    public static void glonkyQuad(VertexConsumer consumer, PoseStack.Pose matrixEntry, int index, float radius, float r, float g, float b, float a, int light)
    {
        Matrix4f matrix4f = matrixEntry.pose();

        for (int i = 0; i < 3; ++i)
        {
            float ang = i / 4f * Mth.PI * 2;
            float u = 0.5f + Mth.sin(ang) + thing(i + index);
            float v = (float) (0.5f + Mth.cos(ang) );
            float x = radius * Mth.sin(i / 3f * Mth.PI * 2) + thing(i * index);
            float z = radius * Mth.cos(i / 3f * Mth.PI * 2);

            Vector4f vector4f = matrix4f.transform(new Vector4f(x, 0, z, 1));
            consumer.addVertex(vector4f.x(), vector4f.y(), vector4f.z(), r, g, b, a, u, v, OverlayTexture.NO_OVERLAY, light, 0, 1, 0);
        }
        Vector4f vector4f = matrix4f.transform(new Vector4f(1, 0.5f, 0, 1));
        consumer.addVertex(vector4f.x, vector4f.y, vector4f.z, r, g, b, a, 0, 0, OverlayTexture.NO_OVERLAY, light, 0, 1, 0);
    }

    private static float thing(float f)
    {
        return 4 * Mth.sin(f);
    }

    @Override
    protected boolean hasLabel(HoundEntity livingEntity)
    {
        // For some reason the label is rendered by default.
        return false;
    }

    @Override
    public ResourceLocation getTexture(HoundEntity entity)
    {
        return TEXTURE;
    }
}
