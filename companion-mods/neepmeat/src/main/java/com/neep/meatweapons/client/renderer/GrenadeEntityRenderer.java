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
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.client.MWClient;
import com.neep.meatweapons.client.model.CannonBulletEntityModel;
import com.neep.meatweapons.entity.BounceGrenadeEntity;
import com.neep.meatweapons.entity.CannonBulletEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class GrenadeEntityRenderer extends EntityRenderer<BounceGrenadeEntity>
{
    public EntityModel<CannonBulletEntity> model;

    public GrenadeEntityRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        model = new CannonBulletEntityModel(context.bakeLayer(MWClient.MODEL_CANNON_BULLET_LAYER));
    }

    @Override
    public void render(BounceGrenadeEntity cannonBulletEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i)
    {
//        matrixStack.push();
//        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(g, cannonBulletEntity.prevYaw, cannonBulletEntity.getYaw()) - 90.0F));
//        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(g, cannonBulletEntity.prevPitch, cannonBulletEntity.getPitch())));
//        model.render(matrixStack, vertexConsumerProvider.getBuffer(model.getLayer(getTexture(cannonBulletEntity))), i, 1, 1.0F, 1.0F, 1.0F, 1F);
//        matrixStack.pop();
    }

    public ResourceLocation getTexture(BounceGrenadeEntity entity)
    {
        // TODO: change
        return ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "textures/entity/explosive_shell.png");
    }
}
