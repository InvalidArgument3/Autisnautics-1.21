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
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

@SuppressWarnings("UnstableApiUsage")
public class AlembicRenderer implements BlockEntityRenderer<AlembicBlockEntity>
{
    public AlembicRenderer(BlockEntityRendererProvider.Context ctx)
    {

    }

    @Override
    public void render(AlembicBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        WritableSingleFluidStorage storage = be.getStorage(null);
        storage.renderLevel = (float) Mth.lerp(0.1, storage.renderLevel, storage.amount / (float) storage.getCapacity());
        renderFluidCuboid(vertexConsumers, matrices, storage.getResource(), 3 / 16f, 3 / 16f, 15 / 16f, storage.renderLevel);
    }

    // Oh gawd, another implementation of this. I should sit down and try to make a generic one, but I just can't...
    public static void renderFluidCuboid(MultiBufferSource vertices, PoseStack matrices, FluidVariant fluid, float startXYZ, float depth, float endY, float scaleY)
    {
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
        VertexConsumer consumer = vertices.getBuffer(ItemBlockRenderTypes.getRenderType(Blocks.BLACK_STAINED_GLASS.defaultBlockState(), false));
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        int col = FluidVariantRendering.getColor(fluid);

        // Magic colourspace transformation copied from Modern Industrialisation
        float r = ((col >> 16) & 255) / 256f;
        float g = ((col >> 8) & 255) / 256f;
        float b = (col & 255) / 256f;

        if (fluid.isBlank() || scaleY == 0)
        {
            return;
        }

        float startY = startXYZ;
        float dist = startY + (endY - startY) * scaleY;

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

            emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            consumer.putBulkData(matrices.last(), emitter.toBakedQuad(0, sprite, false), r, g, b, 0x00F0_00F0, OverlayTexture.NO_OVERLAY);
        }
    }
}
