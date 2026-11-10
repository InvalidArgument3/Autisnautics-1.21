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
import com.mojang.math.Axis;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.client.MWClient;
import com.neep.meatweapons.client.model.PlasmaEntityModel;
import com.neep.meatweapons.entity.FusionBlastEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class PlasmaEntityRenderer extends EntityRenderer<FusionBlastEntity>
{
    public EntityModel<FusionBlastEntity> model;
    public PlasmaEntityRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        model = new PlasmaEntityModel(context.bakeLayer(MWClient.MODEL_PLASMA_LAYER));
    }

    protected final float MIN_SCALE = 2f;
    protected final float MAX_SCALE = 6f;

    @Override
    public void render(FusionBlastEntity plasmaEntity, float f, float tickDelta, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light)
    {
        float angle = (plasmaEntity.tickCount + tickDelta) * 30;
        float scale = Mth.lerp(plasmaEntity.getPower() / FusionBlastEntity.MAX_POWER, MIN_SCALE, MAX_SCALE);

        matrixStack.pushPose();
        matrixStack.scale(scale, scale , scale);

        matrixStack.mulPose(Axis.YP.rotationDegrees(angle));
        matrixStack.mulPose(Axis.XP.rotationDegrees(angle));
//        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(tickDelta, plasmaEntity.prevYaw, plasmaEntity.getYaw()) - 90.0F));
//        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(tickDelta, plasmaEntity.prevPitch, plasmaEntity.getPitch())));
        model.renderToBuffer(matrixStack, vertexConsumerProvider.getBuffer(model.renderType(getTexture(plasmaEntity))), light, 1, 1.0F, 1.0F, 1.0F, 0.5F);
        matrixStack.popPose();
    }

    @Override
    public ResourceLocation getTexture(FusionBlastEntity entity) {
        return new ResourceLocation(MeatWeapons.NAMESPACE, "textures/entity/plasma.png");
    }
}
