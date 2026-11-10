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
import com.neep.neepmeat.transport.block.fluid_transport.entity.FilterPipeBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

@Environment(value = EnvType.CLIENT)
public class StopValveRenderer<T extends FilterPipeBlockEntity> implements BlockEntityRenderer<T>
{
    public StopValveRenderer(BlockEntityRendererProvider.Context context)
    {

    }

    @Override
    public void render(T be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
//        Direction facing = be.getCachedState().get(AbstractAxialPipe.FACING);
//        boolean open = be.getCachedState().get(StopValveBlock.OPEN);
//
        matrices.pushPose();
//        BERenderUtils.rotateFacingSouth(facing, matrices);
//
//
//        be.openDelta = (float) MathHelper.lerp(0.1, be.openDelta, open ? 0 : 1);
//        matrices.translate(0, be.openDelta * - 1 / 16f, 0);
//
//        matrices.translate(0.5, 0.5, 0.5);
//        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(be.openDelta * 135));
//        matrices.translate(-0.5, -0.5, -0.5);
//
//        BERenderUtils.renderModel(NMExtraModels.VALVE_WHEEL, matrices, be.getWorld(), be.getPos(), be.getCachedState(), vertexConsumers);
//
        matrices.popPose();
    }

}
