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

package com.neep.neepmeat.machine.death_blades;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.client.renderer.BERenderUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

@Environment(value = Dist.CLIENT)
public class DeathBladesRenderer implements BlockEntityRenderer<DeathBladesBlockEntity>
{
    public DeathBladesRenderer(BlockEntityRendererProvider.Context ctx)
    {

    }

    @Override
    public void render(DeathBladesBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
//        float angle = MathHelper.wrapDegrees(be.getWorld().getTime() + tickDelta);

        matrices.pushPose();
        Direction facing = be.getBlockState().getValue(DeathBladesBlock.FACING);
        float angle = Mth.rotLerp(tickDelta, be.clientAngle, be.angle);
        renderBlade(be, matrices, facing, vertexConsumers, angle, 0);
        renderBlade(be, matrices, facing, vertexConsumers, 360 - angle, 2);
        matrices.popPose();
    }

    public static void renderBlade(DeathBladesBlockEntity be, PoseStack matrices, Direction facing, MultiBufferSource vertexConsumers, float angle, float offset)
    {
        matrices.pushPose();
        BERenderUtils.rotateFacing(facing, matrices);
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.ZP.rotationDegrees(angle));
        matrices.translate( 0, 0, - offset / 16f);
        matrices.translate(-0.5, -0.5, -0.5);
        BERenderUtils.renderModel(NMExtraModels.LARGE_BLADE, matrices, be.getLevel(), be.getBlockPos(), be.getBlockState(), vertexConsumers);
        matrices.popPose();
    }
}
