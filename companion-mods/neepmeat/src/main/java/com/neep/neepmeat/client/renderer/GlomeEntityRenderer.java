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

package com.neep.neepmeat.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.model.GlomeEntityModel;
import com.neep.neepmeat.entity.GlomeEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class GlomeEntityRenderer extends LivingEntityRenderer<GlomeEntity, GlomeEntityModel>
{
    public GlomeEntityRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer)
    {
        super(ctx, new GlomeEntityModel(ctx.bakeLayer(layer)), 0.3f);
    }

    @Override
    public void render(GlomeEntity entity, float f, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumerProvider, int i)
    {
        float a = entity.tickCount / 20f;
        float b = tickDelta / 20;
        float sin = (float) (Math.sin(a) * Math.cos(b) + Math.cos(a) * Math.sin(b));

        float easeEnvelope = (float) (1f - Math.pow((entity.tickCount + tickDelta) / (float) entity.getMaxTime(), 3f));

        float scale1 = Mth.lerp((sin + 1f) / 2f * easeEnvelope, 0.2f, 1f);

        float angle = (entity.getInitialRandom() + entity.tickCount + tickDelta) * 10f;

        matrices.pushPose();
        matrices.translate(0, entity.getBbHeight() / 2, 0);
        matrices.mulPose(Axis.YP.rotationDegrees(angle));
        matrices.mulPose(Axis.XP.rotationDegrees(angle));
        matrices.scale(scale1, scale1, scale1);
        matrices.translate(0, -entity.getBbHeight() / 2, 0);
        super.render(entity, f, b, matrices, vertexConsumerProvider, i);
        matrices.popPose();
    }

    @Nullable
    @Override
    protected RenderType getRenderLayer(GlomeEntity entity, boolean showBody, boolean translucent, boolean showOutline)
    {
        return RenderType.entityTranslucent(getTexture(entity));
    }

    @Override
    public ResourceLocation getTextureLocation(GlomeEntity entity)
    {
        return ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/entity/glome.png");
    }

    @Override
    protected void renderLabelIfPresent(GlomeEntity entity, Component text, PoseStack matrices, MultiBufferSource vertexConsumers, int light)
    {
//        super.renderLabelIfPresent(entity, text, matrices, vertexConsumers, light);
    }
}
