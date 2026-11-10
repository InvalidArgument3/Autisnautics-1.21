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

package com.neep.neepmeat.machine.charnel_pump;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.neep.neepmeat.init.NMFluids;
import com.neep.meatlib.client.renderer.Renderer;
import com.neep.meatlib.client.renderer.RendererAccess;
import com.neep.meatlib.client.renderer.mesh.QuadEmitter;
import com.neep.meatlib.client.fluid.FluidClient.FluidVariantRendering;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

public class CharnelPumpRenderer implements BlockEntityRenderer<CharnelPumpBlockEntity>
{
    public CharnelPumpRenderer(BlockEntityRendererProvider.Context context)
    {
    }

    public static float plungerAnimation(CharnelPumpBlockEntity be, float t)
    {
        float s;
        if (CharnelPumpBlockEntity.isRising(t))
        {
            s = easeInOutSine(t / 60);
        }
        else
        {
            s = 1 - (t - 60) / 40;
        }
        return s;
    }

    public static float easeInOutSine(float x)
    {
        return (float) (-(Math.cos(Math.PI * x) - 1) / 2);
    }

    @Override
    public void render(CharnelPumpBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vcp, int light, int overlay)
    {
        int upLight = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above(1));

//        float s = (NMMaths.sin(be.getWorld().getTime(), AnimationTickHolder.getPartialTicks(), 0.1f) + 1) / 2;
        float t = be.animationTicks > 0 ? 100 - be.animationTicks + tickDelta * be.progressIncrement() : 0;

        float height = 0;
        if (be.hasFluidCurrentCycle)
            height = 3 * plungerAnimation(be, t) + 4 / 16f;

        FluidVariant fluidVariant = NMFluids.WORK_FLUID.variant();
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluidVariant);
        if (sprite == null)
            return;

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        assert renderer != null;
        QuadEmitter emitter = renderer.meshBuilder().getEmitter();

        VertexConsumer consumer = vcp.getBuffer(ItemBlockRenderTypes.getRenderType(Blocks.BLACK_STAINED_GLASS.defaultBlockState(), false));

        matrices.translate(0, 3, 0);
        matrices.translate(0.5, 0, 0.5);
        matrices.mulPose(Axis.XP.rotationDegrees(-90));
        renderFluidColumn(height, 0.9f, matrices, sprite, emitter, consumer, upLight);
    }

    private static void renderFluidColumn(float height, float radius, PoseStack matrices, TextureAtlasSprite sprite, QuadEmitter emitter, VertexConsumer consumer, int light)
    {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        float k = 0.0F;
        float m = 0.0F;
        float l = radius;
        float g = height;

        float cr = 1;
        float cg = 1;
        float cb = 1;

        for (int seg = 1; seg <= 8; ++seg)
        {
            float o = Mth.sin(seg * 2 * Mth.PI / 8.0F) * radius;
            float p = Mth.cos(seg * 2 * Mth.PI / 8.0F) * radius;
            float q = seg / 8.0F;

            emitter.pos(0, new Vector3f(k, l, 0)).uv(0, u1, v0);
            emitter.pos(1, new Vector3f(k, l, g)).uv(1, u0, v0);
            emitter.pos(2, new Vector3f(o, p, g)).uv(2, u0, v1);
            emitter.pos(3, new Vector3f(o, p, 0)).uv(3, u1, v1);
            BakedQuad quad = emitter.toBakedQuad(sprite);
            consumer.putBulkData(matrices.last(), quad, cr, cg, cb, light, OverlayTexture.NO_OVERLAY);

            k = o;
            l = p;
        }
    }
}
