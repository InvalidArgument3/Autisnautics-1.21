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
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.meatlib.transfer.MultiFluidBuffer;
import com.neep.meatlib.client.renderer.Renderer;
import com.neep.meatlib.client.renderer.RendererAccess;
import com.neep.meatlib.client.renderer.mesh.MutableQuadView;
import com.neep.meatlib.client.renderer.mesh.QuadEmitter;
import com.neep.meatlib.client.fluid.FluidClient.FluidVariantRendering;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class MultiFluidRenderer
{
    public static void renderMultiFluid(MultiFluidBuffer multi, float radius, float maxHeight, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        float start = 0;
        matrices.translate(0.05, 0, 0.05);
        matrices.scale(2 * radius - 0.1f, maxHeight, 2 * radius - 0.1f);
        int max = 0;
        for (Map.Entry<FluidVariant, Long> slot : multi.getSlots())
        {
            float height = slot.getValue() / (float) multi.getCapacity();
            renderFluidCuboid(vertexConsumers, matrices,
                    slot.getKey(),
                    start,
                    start + height,
                    start + height,
                    1, light);
            start += height;
            ++max;
        }
//        System.out.println(max);
    }

    public static void renderFluidCuboid(MultiBufferSource vertices, PoseStack matrices, FluidVariant fluid, float startXYZ, float endXZ, float endY, float scaleY, int light)
    {
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
        VertexConsumer consumer = vertices.getBuffer(ItemBlockRenderTypes.getRenderType(Blocks.BLACK_STAINED_GLASS.defaultBlockState(), false));
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        int col = FluidVariantRendering.getColor(fluid);

        float r = ((col >> 16) & 255) / 256f;
        float g = ((col >> 8) & 255) / 256f;
        float b = (col & 255) / 256f;

        if (fluid.isBlank() || scaleY == 0)
        {
            return;
        }

        float startY = startXYZ;
        float dist = startY + (endY - startY) * scaleY;

        float depth = 1f;

        assert renderer != null;
        QuadEmitter emitter = renderer.meshBuilder().getEmitter();

        for (Direction direction : Direction.values())
        {
            if (direction == Direction.UP)
            {
                emitter.square(Direction.UP, depth, depth, 1 - depth, 1 - depth, 1 - dist);
            }
            else if (direction != Direction.DOWN)
            {
                emitter.square(direction, depth, startY, 1 - depth, dist, depth);
            }

            if (sprite != null)
            {
                emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
                emitter.spriteColor(0, -1, -1, -1, -1);
            }

            consumer.putBulkData(matrices.last(), emitter.toBakedQuad(0, sprite, false), r, g, b, light, OverlayTexture.NO_OVERLAY);
        }
    }
}
