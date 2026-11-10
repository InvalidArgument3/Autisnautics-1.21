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
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.model.entity.WormEntityModel;
import com.neep.neepmeat.entity.worm.WormEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WormEntityRenderer extends GeoEntityRenderer<WormEntity>
{
    protected static final ResourceLocation TEXTURE = new ResourceLocation(NeepMeat.NAMESPACE, "block/duat_stone.png");

    public WormEntityRenderer(EntityRendererProvider.Context ctx)
    {
        super(ctx, new WormEntityModel());
    }

    @Override
    public void render(WormEntity entity, float entityYaw, float tickDelta, PoseStack matrices, MultiBufferSource vcp, int packedLight)
    {
//        setCurrentModelRenderCycle(EModelRenderCycle.INITIAL);
//        matrices.push();
//
//        this.dispatchedMat = matrices.peek().getPositionMatrix().copy();
//
//        float lerpHeadRot = MathHelper.lerpAngleDegrees(tickDelta, entity.prevHeadYaw, entity.headYaw);
//        float headPitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch());
//
//        float ageInTicks = entity.age + tickDelta;
//        float limbSwingAmount = 0;
//        float limbSwing = 0;
//
//        applyRotations(entity, matrices, headPitch, lerpHeadRot, tickDelta);
//
//        GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelLocation(entity));
//
//        this.modelProvider.setCustomAnimations(entity, getInstanceId(entity));
//
//        matrices.translate(0, 0.01f, 0);
//        RenderSystem.setShaderTexture(0, getTextureLocation(entity));
//
//        Color renderColor = getRenderColor(entity, tickDelta, matrices, vcp, null, packedLight);
//        RenderLayer renderType = getRenderType(entity, tickDelta, matrices, vcp, null, packedLight,
//                getTextureLocation(entity));
//
//        VertexConsumer glintBuffer = vcp.getBuffer(RenderLayer.getDirectEntityGlint());
//        VertexConsumer translucentBuffer = vcp
//                .getBuffer(RenderLayer.getEntityTranslucentCull(getTextureLocation(entity)));
//
//        render(model, entity, tickDelta, renderType, matrices, vcp,
//                glintBuffer != translucentBuffer ? VertexConsumers.union(glintBuffer, translucentBuffer)
//                        : null,
//                packedLight, getOverlay(entity, 0), renderColor.getRed() / 255f,
//                renderColor.getGreen() / 255f, renderColor.getBlue() / 255f,
//                renderColor.getAlpha() / 255f);
//
//        for (GeoLayerRenderer<WormEntity> layerRenderer : this.layerRenderers)
//        {
//            renderLayer(matrices, vcp, packedLight, entity, limbSwing, limbSwingAmount, tickDelta, ageInTicks,
//                    0, headPitch, vcp, layerRenderer);
//        }
//
//        matrices.pop();

        renderSegment(entity, entity, matrices, tickDelta, vcp, packedLight);

        for (WormEntity.WormSegment segment : entity.getParts())
        {
            matrices.pushPose();
            matrices.translate(
                    Mth.lerp(tickDelta, segment.xo - entity.getX(), segment.getX() - entity.getX()),
                    Mth.lerp(tickDelta, segment.yo - entity.getY(), segment.getY() - entity.getY()),
                    Mth.lerp(tickDelta, segment.zo - entity.getZ(), segment.getZ() - entity.getZ()));
            renderSegment(entity, segment, matrices, tickDelta, vcp, packedLight);
            matrices.popPose();

            segment.xOld = segment.getX();
            segment.yOld = segment.getY();
            segment.zOld = segment.getZ();
        }
    }

    protected void renderSegment(WormEntity base, Entity segment, PoseStack matrices, float tickDelta, MultiBufferSource vcp, int packedLight)
    {
        matrices.pushPose();

//        this.dispatchedMat = matrices.peek().getPositionMatrix();
//
//        float lerpYaw = segment.getYaw();
//        float lerpPitch = segment.getPitch();
//
//        float ageInTicks = base.age + tickDelta;
//
//        applyRotations(segment, matrices, lerpPitch, lerpYaw, tickDelta);
//
//        GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelLocation(base));
//
//        this.modelProvider.setCustomAnimations(base, getInstanceId(base));
//
//        matrices.translate(0, 0.01f, 0);
//        RenderSystem.setShaderTexture(0, getTextureLocation(base));
//
//        Color renderColor = getRenderColor(base, tickDelta, matrices, vcp, null, packedLight);
//        RenderLayer renderType = getRenderType(base, tickDelta, matrices, vcp, null, packedLight,
//                getTextureLocation(base));
//
//        VertexConsumer glintBuffer = vcp.getBuffer(RenderLayer.getDirectEntityGlint());
//        VertexConsumer translucentBuffer = vcp
//                .getBuffer(RenderLayer.getEntityTranslucentCull(getTextureLocation(base)));
//
//        render(model, base, tickDelta, renderType, matrices, vcp,
//                glintBuffer != translucentBuffer ? VertexConsumers.union(glintBuffer, translucentBuffer)
//                        : null,
//                packedLight, getOverlay(base, 0), renderColor.getRed() / 255f,
//                renderColor.getGreen() / 255f, renderColor.getBlue() / 255f,
//                renderColor.getAlpha() / 255f);
//
//        for (GeoLayerRenderer<WormEntity> layerRenderer : this.layerRenderers)
//        {
//            renderLayer(matrices, vcp, packedLight, base, 0, 0, tickDelta, ageInTicks,
//                    0, lerpPitch, vcp, layerRenderer);
//        }

        matrices.popPose();

    }

    @Override
    public boolean shouldRender(WormEntity entity, Frustum frustum, double x, double y, double z)
    {
        return super.shouldRender(entity, frustum, x, y, z);
    }

//    protected void applyRotations(Entity entity, MatrixStack matrices, float pitch, float yaw, float tickDelta)
//    {
//        matrices.translate(0, 0.5, 0);
//        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f - yaw));
//        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f - pitch));
//        matrices.translate(0, -0.5, 0);
//    }

    @Override
    public ResourceLocation getTexture(WormEntity entity)
    {
        return TEXTURE;
    }
}
