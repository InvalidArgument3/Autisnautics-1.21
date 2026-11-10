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
import com.neep.meatweapons.client.model.CannonBulletEntityModel;
import com.neep.meatweapons.entity.CannonBulletEntity;
import com.neep.meatweapons.entity.ExplodingShellEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ShellEntityRenderer<T extends ExplodingShellEntity> extends EntityRenderer<T>
{
    public EntityModel<CannonBulletEntity> model;


    public ShellEntityRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        model = new CannonBulletEntityModel(context.bakeLayer(MWClient.MODEL_CANNON_BULLET_LAYER));
    }

    @Override
    public void render(T entity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i)
    {
        matrixStack.pushPose();
        matrixStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(g, entity.yRotO, entity.getYRot()) - 90.0F));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(g, entity.xRotO, entity.getXRot())));
        model.renderToBuffer(matrixStack, vertexConsumerProvider.getBuffer(model.renderType(getTexture(entity))), 0, i, 1.0F, 1.0F, 1.0F, 1F);
        matrixStack.popPose();
    }

    @Override
    public ResourceLocation getTexture(T entity)
    {
        return ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "textures/entity/explosive_shell.png");
    }
}
