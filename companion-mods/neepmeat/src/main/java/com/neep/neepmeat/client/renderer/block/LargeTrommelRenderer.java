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
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.neep.neepmeat.machine.live_machine.block.LargeTrommelBlock;
import com.neep.neepmeat.machine.live_machine.block.entity.LargeTrommelBlockEntity;
import com.neep.meatlib.client.renderer.Renderer;
import com.neep.meatlib.client.renderer.RendererAccess;
import com.neep.meatlib.client.renderer.mesh.MeshBuilder;
import com.neep.meatlib.client.renderer.mesh.QuadEmitter;
import com.neep.meatlib.client.fluid.FluidClient.FluidVariantRendering;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

public class LargeTrommelRenderer implements BlockEntityRenderer<LargeTrommelBlockEntity>
{
    public LargeTrommelRenderer(BlockEntityRendererProvider.Context context)
    {

    }

    @Override
    public void render(LargeTrommelBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vcp, int light, int overlay)
    {
        FluidVariant fluidVariant = be.getStorage().variant;
        Direction facing = be.getBlockState().getValue(LargeTrommelBlock.FACING);
        if (!fluidVariant.isBlank() && be.progressIncrement() > 0)
        {
            float progress = (be.getLevel().getGameTime() - be.getStorage().recipeStartTime) / (be.getStorage().totalProgress / be.progressIncrement());

            matrices.translate(0.5, 0.5, 0.5);
            matrices.mulPose(Axis.YN.rotationDegrees(facing.toYRot()));
            matrices.translate(-0.5, -0.5, -0.5);
            matrices.translate(-0.5, -0.2, 1);
            renderFluidColumn(vcp, matrices, fluidVariant, 1 - progress, light);
        }
    }

    public static void renderFluidColumn(MultiBufferSource vcp, PoseStack matrices, FluidVariant fluid, float level, int light)
    {
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
        if (sprite == null)
            return;

        VertexConsumer consumer = vcp.getBuffer(ItemBlockRenderTypes.getRenderType(Blocks.BLACK_STAINED_GLASS.defaultBlockState(), false));

        int col = FluidVariantRendering.getColor(fluid);

        float cr = ((col >> 16) & 255) / 256f;
        float cg = ((col >> 8) & 255) / 256f;
        float cb = (col & 255) / 256f;

        if (fluid.isBlank() || level == 0)
        {
            return;
        }

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        assert renderer != null;
        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();

        float y0 = 0.7f;
        float y1 = 0.5f + 0.7f * level;
        float x0 = -0.2f;
        float x1 = 1.2f;
        float x2 = -0.4f;
        float x3 = 1.4f;

        // Needs three sections to cover the entire length without stretching the texture.
        for (int i = 0; i < 3; ++i)
        {
            float z = i;

            // Top face
            emitter.pos(0, new Vector3f(x1, y1, z + 0)).uv(0, u1, v0);
            emitter.pos(1, new Vector3f(x0, y1, z + 0)).uv(1, u0, v0);
            emitter.pos(2, new Vector3f(x0, y1, z + 1)).uv(2, u0, v1);
            emitter.pos(3, new Vector3f(x1, y1, z + 1)).uv(3, u1, v1);
            BakedQuad quad = emitter.toBakedQuad(sprite);
            consumer.putBulkData(matrices.last(), quad, cr, cg, cb, 1.0F, light, OverlayTexture.NO_OVERLAY);
            emitter.emit();

            // Right face (looking from the trommel's controller block)
            emitter.pos(0, new Vector3f(x0, y1, z + 0)).uv(0, u1, v0);
            emitter.pos(1, new Vector3f(x2, y0, z + 0)).uv(1, u0, v0);
            emitter.pos(2, new Vector3f(x2, y0, z + 1)).uv(2, u0, v1);
            emitter.pos(3, new Vector3f(x0, y1, z + 1)).uv(3, u1, v1);
            quad = emitter.toBakedQuad(sprite);
            consumer.putBulkData(matrices.last(), quad, cr, cg, cb, 1.0F, light, OverlayTexture.NO_OVERLAY);
            emitter.emit();

            // Left face
            emitter.pos(1, new Vector3f(x1, y1, z + 0)).uv(1, u1, v0);
            emitter.pos(0, new Vector3f(x3, y0, z + 0)).uv(0, u0, v0);
            emitter.pos(3, new Vector3f(x3, y0, z + 1)).uv(3, u0, v1);
            emitter.pos(2, new Vector3f(x1, y1, z + 1)).uv(2, u1, v1);
            quad = emitter.toBakedQuad(sprite);
            consumer.putBulkData(matrices.last(), quad, cr, cg, cb, 1.0F, light, OverlayTexture.NO_OVERLAY);
            emitter.emit();
        }
    }
}
