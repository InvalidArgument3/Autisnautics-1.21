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

package com.neep.neepmeat.transport.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.neepmeat.transport.block.fluid_transport.entity.FluidGaugeBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class FluidGaugeBlockRenderer implements BlockEntityRenderer<FluidGaugeBlockEntity>
{
    public FluidGaugeBlockRenderer(BlockEntityRendererProvider.Context ctx)
    {

    }

    @Override
    public void render(FluidGaugeBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
//        matrices.push();
//
//        BERenderUtils.rotateFacing(be.getCachedState().get(FluidGaugeBlock.FACING), matrices);
//
//
//        be.getLastFluid();
//        Sprite sprite = FluidVariantRendering.getSprite(fluid);
//        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());
//        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
//
//        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
//        QuadEmitter emitter = renderer.meshBuilder().getEmitter();
//
//        emitter.
//
//        matrices.pop();
    }
}
