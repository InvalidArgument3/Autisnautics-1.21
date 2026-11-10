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
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.entity.MobEggEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

@OnlyIn(Dist.CLIENT)
public class EggEntityRenderer extends EntityRenderer<MobEggEntity>
{
    public EggEntityRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer)
    {
        super(ctx);
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(MobEggEntity entity, float f, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumerProvider, int i)
    {
        float b = tickDelta / 20;
        matrices.pushPose();

        float p = (float) entity.getWobbleTicks() - tickDelta;
        float q = entity.getWobbleStrength() - tickDelta;
        if (q < 0.0f)
        {
            q = 0.0f;
        }
        if (p > 0.0f)
        {
            matrices.mulPose(Axis.XP.rotationDegrees(Mth.sin(p) * p * q / 10.0f * entity.getWobbleDirection()));
        }

        matrices.translate(-0.5, 0, -0.5);
        BERenderUtils.renderModel(NMExtraModels.EGG, matrices, entity.level(), entity.blockPosition(), Blocks.STONE.defaultBlockState(), vertexConsumerProvider);
        matrices.popPose();
        super.render(entity, f, b, matrices, vertexConsumerProvider, i);
    }

    @Override
    public ResourceLocation getTexture(MobEggEntity entity)
    {
        return ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/entity/glome.png");
    }
}
