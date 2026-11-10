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
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.neepmeat.client.renderer.BERenderUtils;
import com.neep.neepmeat.transport.block.fluid_transport.WindowPipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.entity.WindowPipeBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.neep.meatlib.client.renderer.Renderer;
import com.neep.meatlib.client.renderer.RendererAccess;
import com.neep.meatlib.client.renderer.mesh.MutableQuadView;
import com.neep.meatlib.client.renderer.mesh.QuadEmitter;
import com.neep.meatlib.client.fluid.FluidClient.FluidVariantRendering;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

@OnlyIn(Dist.CLIENT)
public class WindowPipeRenderer implements BlockEntityRenderer<WindowPipeBlockEntity>
{
    public WindowPipeRenderer(BlockEntityRendererProvider.Context context)
    {
    }

    @Override
    public void render(WindowPipeBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        WindowPipeBlockEntity.WindowFluidPipeVertex vertex = be.getPipeVertex();

        if (be.clientVariant.isBlank() && !be.lastClientVariant.isBlank() && be.clientFraction <= 0.05)
            be.lastClientVariant = FluidVariant.BLANK; // Wait for the fraction to smoothly approach zero before stopping rendering
        else if (!be.clientVariant.isBlank())
            be.lastClientVariant = be.clientVariant;

        be.clientFraction = Mth.lerp(0.1f, be.clientFraction, (float) be.clientAmount / vertex.getCapacity());

        var facing = be.getBlockState().getValue(WindowPipeBlock.FACING);
        BERenderUtils.rotateFacing(facing, matrices);
        if (facing.getAxis() == Direction.Axis.Y)
            renderVertical(vertexConsumers, matrices, be.lastClientVariant, 5.3f / 16f, 5.3f / 16f, be.clientFraction, light);
        else
            renderFluidCuboid(vertexConsumers, matrices, be.lastClientVariant, 5.3f / 16f, 5.3f / 16f, be.clientFraction, light);
    }

    // Shows fluid sitting in the bottom of the pipe
    public static void renderFluidCuboid(MultiBufferSource vertices, PoseStack matrices, FluidVariant fluid, float width, float length, float fraction, int light)
    {
        if (fluid.isBlank() || fraction == 0)
            return;

        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
        VertexConsumer consumer = vertices.getBuffer(RenderType.translucent());
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        int col = FluidVariantRendering.getColor(fluid);

        float r = ((col >> 16) & 255) / 256f;
        float g = ((col >> 8) & 255) / 256f;
        float b = (col & 255) / 256f;

        QuadEmitter emitter = renderer.meshBuilder().getEmitter();

        for (Direction direction : Direction.values())
        {
            float pipeBottom = 0.5f - width / 2f;
            if (direction == Direction.WEST || direction == Direction.EAST)
            {
                emitter.square(direction, 0, pipeBottom, 1, pipeBottom + width * fraction, width);
            }
            else if (direction == Direction.UP)
            {
                emitter.square(direction, pipeBottom, 0, 1 - pipeBottom, 1, pipeBottom + width * (1 - fraction));
            }
            else if (direction == Direction.DOWN)
            {
                emitter.square(direction, pipeBottom, 0, 1 - pipeBottom, 1, pipeBottom);
            }

            emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);
//            emitter.setColor(-1, -1, -1, -1);
            consumer.putBulkData(matrices.last(), emitter.toBakedQuad(sprite), r, g, b, 1.0F, light, OverlayTexture.NO_OVERLAY);
        }
    }

    public static void renderVertical(MultiBufferSource vertices, PoseStack matrices, FluidVariant fluid, float width, float length, float fraction, int light)
    {
        if (fluid.isBlank() || fraction == 0)
            return;

        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
        VertexConsumer consumer = vertices.getBuffer(RenderType.translucent());
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        int col = FluidVariantRendering.getColor(fluid);

        float r = ((col >> 16) & 255) / 256f;
        float g = ((col >> 8) & 255) / 256f;
        float b = (col & 255) / 256f;


        QuadEmitter emitter = renderer.meshBuilder().getEmitter();

        for (Direction direction : Direction.values())
        {
            if (direction == Direction.WEST || direction == Direction.EAST)
            {
                emitter.square(direction,  1/2f - width / 2, 1/2f - width / 2 * fraction, 1/2f + width / 2, 1/2f + width / 2 * fraction, 1/2f - width / 2 * fraction);
            }
            else if (direction == Direction.UP || direction == Direction.DOWN)
            {
                emitter.square(direction,  1/2f - (length / 2) * fraction, (1 - width) / 2, 1/2f + (length / 2) * fraction, 1/2f + width / 2, 1/2f - length / 2 * fraction);
            }

            emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);
//            emitter.setColor(-1, -1, -1, 10);
            consumer.putBulkData(matrices.last(), emitter.toBakedQuad(sprite), r, g, b, 1.0F, light, OverlayTexture.NO_OVERLAY);
        }
    }
}
