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

package com.neep.neepmeat.machine.multitank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatlib.transfer.MultiFluidBuffer;
import com.neep.neepmeat.client.renderer.MultiFluidRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

@Environment(value = EnvType.CLIENT)
public class MultiTankRenderer implements BlockEntityRenderer<MultiTankBlockEntity>
{
    public MultiTankRenderer(BlockEntityRendererProvider.Context ctx)
    {

    }

    @Override
    public void render(MultiTankBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        MultiFluidBuffer buffer = be.getStorage();
        float maxHeight = 1;

        matrices.pushPose();

        matrices.pushPose();
        matrices.scale(1, 0.9f, 1);
        matrices.translate(0, 0.05, 0);
        MultiFluidRenderer.renderMultiFluid(buffer, 0.5f, maxHeight, matrices, vertexConsumers, light, overlay);
        matrices.popPose();

        float fluidHeight = buffer.getTotalAmount() / (float) buffer.getCapacity() * maxHeight;
        float angleOffset = (float) (Math.PI / 3);
        float angle = be.getLevel().getGameTime() + tickDelta;
        float offset = 0.5f;

//        VertexConsumer consumer = vertexConsumers.getBuffer(BeamEffect.BEAM_LAYER);
//        BeamRenderer.renderBeam(matrices, consumer, MinecraftClient.getInstance().cameraEntity.getEyePos(),
//                new Vec3d(58, 4, 135),
//                new Vec3d(58, 10, 133),
//        123, 171, 354, 100, 1);

        matrices.translate(-0.5, -0.5, -0.5);

        matrices.popPose();
    }
}
