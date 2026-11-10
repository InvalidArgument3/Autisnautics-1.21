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
import net.minecraft.client.resources.model.ModelResourceLocation;


import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.mixin.BlockModelRendererAccessor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.neep.meatlib.client.renderer.Renderer;
import com.neep.meatlib.client.renderer.RendererAccess;
import com.neep.meatlib.client.renderer.mesh.MutableQuadView;
import com.neep.meatlib.client.renderer.mesh.QuadEmitter;
import com.neep.meatlib.client.fluid.FluidClient.FluidVariantRendering;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.FluidVariantAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.BitSet;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class BERenderUtils
{
    public static void renderModelSmooth(ResourceLocation model, PoseStack matrices, Level world, BlockPos pos, MultiBufferSource vertexConsumers)
    {
        ModelManager manager = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getModelManager();
        BakedModel handle = manager.getModel(ModelResourceLocation.standalone(model));
        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();

        renderSmooth(renderer, world, handle, pos, matrices, vertexConsumers.getBuffer(RenderType.cutout()), RandomSource.create(), 0, 0);
    }

    public static void renderSmooth(ModelBlockRenderer renderer, BlockAndTintGetter world, BakedModel model, BlockPos pos, PoseStack matrices, VertexConsumer vertexConsumer, RandomSource random, long seed, int overlay)
    {
        BlockModelRendererAccessor accessor = (BlockModelRendererAccessor) renderer;
        float[] fs = new float[Direction.values().length * 2];
        BitSet bitSet = new BitSet(3);
        ModelBlockRenderer.AmbientOcclusionFace ambientOcclusionCalculator = new ModelBlockRenderer.AmbientOcclusionFace();

        BlockState state = Blocks.AIR.defaultBlockState();

        for (Direction direction : Direction.values())
        {
            random.setSeed(seed);
            List<BakedQuad> list = model.getQuads(state, direction, random);
            if (!list.isEmpty())
            {
                accessor.callRenderQuadsSmooth(world, state, pos, matrices, vertexConsumer, list, fs, bitSet, ambientOcclusionCalculator, overlay);
            }
        }

        random.setSeed(seed);
        List<BakedQuad> list2 = model.getQuads(state, null, random);
        if (!list2.isEmpty())
        {
            accessor.callRenderQuadsSmooth(world, state, pos, matrices, vertexConsumer, list2, fs, bitSet, ambientOcclusionCalculator, overlay);
        }
    }

    public static void renderModel(ResourceLocation model, PoseStack matrices, Level world, BlockPos pos, BlockState state, MultiBufferSource vertexConsumers)
    {
        ModelManager manager = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getModelManager();
        BakedModel handle = manager.getModel(ModelResourceLocation.standalone(model));
        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();

        renderFlat(
                renderer,
                world,
                handle,
                state,
                pos,
                matrices,
                vertexConsumers.getBuffer(RenderType.cutout()),
                true,
                RandomSource.create(),
                0,
                0
        );
    }

    private static void renderFlat(ModelBlockRenderer renderer, BlockAndTintGetter world, BakedModel model, BlockState state, BlockPos pos, PoseStack matrices, VertexConsumer vertexConsumer, boolean cull, net.minecraft.util.RandomSource random, long seed, int overlay)
    {
        BitSet bitSet = new BitSet(3);
        random.setSeed(seed);
        List<BakedQuad> list2 = model.getQuads(state, null, random);
        if (!list2.isEmpty())
        {
            renderQuadsFlat(renderer, world, state, pos, -1, overlay, true, matrices, vertexConsumer, list2, bitSet);
        }
    }

    private static void renderQuadsFlat(ModelBlockRenderer renderer, BlockAndTintGetter world, BlockState state, BlockPos pos, int light, int overlay, boolean useWorldLight, PoseStack matrices, VertexConsumer vertexConsumer, List<BakedQuad> quads, BitSet flags)
    {
        BlockModelRendererAccessor accessor = (BlockModelRendererAccessor) renderer;
        for (BakedQuad bakedQuad : quads)
        {
            if (useWorldLight)
            {
                light = LevelRenderer.getLightColor(world, state, pos);
            }

            float brightness = 1;
            accessor.callRenderQuad(world, state, pos, vertexConsumer, matrices.last(), bakedQuad,
                    brightness, brightness, brightness, brightness,
                    light, light, light, light, overlay);
        }
    }

    public static Direction nearestDirection(Vector3f vec)
    {
        Direction closest = Direction.NORTH;
        float closestDist = 100;
        for (Direction direction : Direction.values())
        {
            Vector3f vec1 = closest.step();
            vec1.sub(vec);
            float len = vec1.length();
            if (len < closestDist)
            {
                closest = direction;
                closestDist = len;
            }
        }
        return Direction.UP;
    }

    /**
     * @param facing   The JSON model must be facing North by default.
     * @param matrices Remember to push and pop.
     */
    public static void rotateFacing(Direction facing, PoseStack matrices)
    {
        matrices.translate(0.5, 0.5, 0.5);
        switch (facing)
        {
            case SOUTH:
            {
                matrices.mulPose(Axis.YP.rotationDegrees(180));
                break;
            }
            case WEST:
            {
                matrices.mulPose(Axis.YP.rotationDegrees(90));
                break;
            }
            case NORTH:
            {
                break;
            }
            case EAST:
            {
                matrices.mulPose(Axis.YP.rotationDegrees(-90));
                break;
            }
            case DOWN:
            {
                matrices.mulPose(Axis.XP.rotationDegrees(-90));
                break;
            }
            case UP:
            {
                matrices.mulPose(Axis.XP.rotationDegrees(90));
                break;
            }
        }
        matrices.translate(-0.5, -0.5, -0.5);
    }

    // For models that are facing the wrong direction
    public static void rotateFacingSouth(Direction facing, PoseStack matrices)
    {
        matrices.translate(0.5, 0.5, 0.5);
        switch (facing)
        {
            case NORTH:
            {
                matrices.mulPose(Axis.YP.rotationDegrees(180));
                break;
            }
            case EAST:
            {
                matrices.mulPose(Axis.YP.rotationDegrees(90));
                break;
            }
            case SOUTH:
            {
                break;
            }
            case WEST:
            {
                matrices.mulPose(Axis.YP.rotationDegrees(-90));
                break;
            }
            case UP:
            {
                matrices.mulPose(Axis.XP.rotationDegrees(-90));
                break;
            }
            case DOWN:
            {
                matrices.mulPose(Axis.XP.rotationDegrees(90));
                break;
            }
        }
        matrices.translate(-0.5, -0.5, -0.5);
    }

    public static void renderFluidCuboid(MultiBufferSource vcp, PoseStack matrices, FluidVariant fluid, float startY, float endY, float depth, float scaleY, int light)
    {
        if (NMFluids.isGas(fluid))
        {
            renderGas(vcp, matrices, fluid, startY, endY, depth, scaleY, light);
            return;
        }

        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
        if (sprite == null)
            return;

        VertexConsumer consumer = vcp.getBuffer(RenderType.translucent());
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        int col = FluidVariantRendering.getColor(fluid);

        float r = ((col >> 16) & 255) / 256f;
        float g = ((col >> 8) & 255) / 256f;
        float b = (col & 255) / 256f;

        if (fluid.isBlank() || scaleY == 0)
        {
            return;
        }

        float dist = startY + (endY - startY) * scaleY;
        if (FluidVariantAttributes.isLighterThanAir(fluid))
        {
            matrices.translate(1, 1, 0);
            matrices.scale(-1, -1, 1);
        }

        QuadEmitter emitter = renderer.meshBuilder().getEmitter();

        for (Direction direction : Direction.values())
        {
            if (direction.getAxis().isVertical())
            {
//                emitter.square(Direction.UP, depth, depth, 1 - depth, 1 - depth, 1 - dist);
                emitter.square(direction, depth, depth, 1 - depth, 1 - depth, direction == Direction.UP ? 1 - dist : startY);
            }
            else if (direction != Direction.DOWN)
            {
                emitter.square(direction, depth, startY, 1 - depth, dist, depth);
            }

            emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            consumer.putBulkData(matrices.last(), emitter.toBakedQuad(0, sprite, false), r, g, b, 1.0F, light, OverlayTexture.NO_OVERLAY);
        }
    }

    public static void renderGas(MultiBufferSource vcp, PoseStack matrices, FluidVariant fluid, float startY, float endY, float depth, float scaleY, int light)
    {
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
        VertexConsumer consumer = vcp.getBuffer(RenderType.translucent());
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        int col = FluidVariantRendering.getColor(fluid);

        float r = ((col >> 16) & 255) / 256f;
        float g = ((col >> 8) & 255) / 256f;
        float b = (col & 255) / 256f;

        if (fluid.isBlank() || scaleY == 0)
            return;

        QuadEmitter emitter = renderer.meshBuilder().getEmitter();

        for (Direction direction : Direction.values())
        {
            if (direction.getAxis().isVertical())
            {
                emitter.square(direction, depth, depth, 1 - depth, 1 - depth, direction == Direction.UP ? 1 - endY : startY);
            }
            else if (direction != Direction.DOWN)
            {
                emitter.square(direction, depth, startY, 1 - depth, endY, depth);
            }

            float alpha = Mth.clamp(scaleY, 0, 1);
            int colWithAlpha = 0x00FFFFFF | (((int) (alpha * 255)) << 24);
            emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
            quad(consumer, matrices.last(), emitter.toBakedQuad(0, sprite, false), 1, r, g, b, alpha, light);
        }
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose matrixEntry, BakedQuad quad, float brightness, float red, float green, float blue, float alpha, int light)
    {
        Vector3f unit = quad.getDirection().step();
        Matrix4f matrix4f = matrixEntry.pose();
        Vector3f vector3f = matrixEntry.normal().transform(unit);

        int[] js = quad.getVertices();
        int j = js.length / 8;

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intBuffer = byteBuffer.asIntBuffer();

            for (int k = 0; k < j; ++k)
            {
                intBuffer.clear();
                intBuffer.put(js, k * 8, 8);
                float f = byteBuffer.getFloat(0);
                float g = byteBuffer.getFloat(4);
                float h = byteBuffer.getFloat(8);
                float o = brightness * red;
                float p = brightness * green;
                float q = brightness * blue;
                float s = brightness * alpha;

                float m = byteBuffer.getFloat(16);
                float n = byteBuffer.getFloat(20);
                Vector4f vector4f = matrix4f.transform(new Vector4f(f, g, h, 1.0F));
                consumer.addVertex(vector4f.x(), vector4f.y(), vector4f.z(), o, p, q, s, m, n, OverlayTexture.NO_OVERLAY, light, vector3f.x(), vector3f.y(), vector3f.z());
            }
        }
    }
}
