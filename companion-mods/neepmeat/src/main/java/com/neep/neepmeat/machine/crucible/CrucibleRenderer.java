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

package com.neep.neepmeat.machine.crucible;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("UnstableApiUsage")
public class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity>
{
    public CrucibleRenderer(BlockEntityRendererProvider.Context ctx)
    {

    }

    @Override
    public void render(CrucibleBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        WritableSingleFluidStorage storage = be.getStorage().fluidStorage;
        storage.renderLevel = (float) Mth.lerp(0.1, storage.renderLevel, storage.amount / (float) storage.getCapacity());
        renderSurface(vertexConsumers, matrices, be.getStorage().fluidStorage.getResource(), light, overlay, 4 / 16f, 11 / 16f, 2 / 16f, storage.renderLevel);
    }

    public static void renderSurface(MultiBufferSource vertices, PoseStack matrices, FluidVariant fluid, int light, int overlay, float start, float height, float depth, float scale)
    {
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
        VertexConsumer consumer = vertices.getBuffer(RenderType.translucent());
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        int col = FluidVariantRendering.getColor(fluid);

        // Magic colourspace transformation copied from Modern Industrialisation
        float r = ((col >> 16) & 255) / 256f;
        float g = ((col >> 8) & 255) / 256f;
        float b = (col & 255) / 256f;

        if (fluid.isBlank() || scale == 0)
        {
            return;
        }

        QuadEmitter emitter = renderer.meshBuilder().getEmitter();

        emitter.square(Direction.UP, depth, depth, 1 - depth, 1 - depth, 1 - (start + height * scale));

        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
        emitter.spriteColor(0, -1, -1, -1, -1);
        consumer.putBulkData(matrices.last(), emitter.toBakedQuad(0, sprite, false), r, g, b, light, overlay);
    }
}
