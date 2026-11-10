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

package com.neep.neepmeat.machine.advanced_motor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.client.renderer.BERenderUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

@Environment(value = EnvType.CLIENT)
public class AdvancedMotorRenderer implements BlockEntityRenderer<AdvancedMotorBlockEntity>
{
    public AdvancedMotorRenderer(BlockEntityRendererProvider.Context ctx)
    {
    }

    @Override
    public void render(AdvancedMotorBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        matrices.pushPose();
        Direction facing = be.getBlockState().getValue(BaseFacingBlock.FACING);

        float delta = Minecraft.getInstance().isPaused() ? 0 : Minecraft.getInstance().getDeltaFrameTime();
        be.currentSpeed = (float) (be.rotorSpeed * Mth.lerp(0.1, be.currentSpeed, be.getSpeed()));
        be.angle = Mth.wrapDegrees(be.angle + be.currentSpeed * delta);

        BERenderUtils.rotateFacingSouth(facing, matrices);
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.ZP.rotationDegrees(be.angle));
        matrices.translate(-0.5, -0.5, -0.5);
        BERenderUtils.renderModel(NMExtraModels.MOTOR_ROTOR, matrices, be.getLevel(), be.getBlockPos(), be.getBlockState(), vertexConsumers);

        matrices.popPose();
    }
}
