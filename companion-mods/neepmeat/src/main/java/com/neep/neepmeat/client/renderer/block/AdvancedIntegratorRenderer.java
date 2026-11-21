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

package com.neep.neepmeat.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.client.renderer.BERenderUtils;
import com.neep.neepmeat.machine.advanced_integrator.AdvancedIntegratorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class AdvancedIntegratorRenderer implements BlockEntityRenderer<AdvancedIntegratorBlockEntity>
{
    public AdvancedIntegratorRenderer(BlockEntityRendererProvider.Context context)
    {

    }

    @Override
    public void render(AdvancedIntegratorBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        long time = entity.getLevel().getGameTime();
        double div = 20;
        float offset = (float) (Math.sin(time / div) * Math.cos(tickDelta / div) + Math.sin(tickDelta / div) * Math.cos(time / div));
        matrices.pushPose();
        matrices.translate(0, 4 + offset * 0.1, 0);
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(45));
        matrices.translate(-0.5, -0.5, -0.5);
        matrices.scale(1, 2, 1);

        BERenderUtils.renderModelSmooth(
                NMExtraModels.ADVANCED_INTEGRATOR_MEAT,
                matrices,
                entity.getLevel(),
                entity.getBlockPos().above(2),
                vertexConsumers
        );
        matrices.popPose();

        matrices.translate(0, 1, 0);
        matrices.scale(1, 1 + offset * 0.05f, 1);
        matrices.translate(0, -1, 0);
        matrices.translate(0, 2, 0);
        BERenderUtils.renderModelSmooth(
                NMExtraModels.ADVANCED_INTEGRATOR_DANGLIES,
                matrices,
                entity.getLevel(),
                entity.getBlockPos().above(2),
                vertexConsumers
        );
    }
}
