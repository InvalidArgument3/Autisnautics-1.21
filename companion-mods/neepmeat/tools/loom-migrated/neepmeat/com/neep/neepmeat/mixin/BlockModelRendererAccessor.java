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

package com.neep.neepmeat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.BitSet;
import java.util.List;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ModelBlockRenderer.class)
public interface BlockModelRendererAccessor
{
    @Invoker("getQuadDimensions")
    void callGetQuadDimensions(BlockAndTintGetter world, BlockState state, BlockPos pos, int[] vertexData, Direction face, float[] box, BitSet flags);

    @Invoker("renderQuad")
    void callRenderQuad(BlockAndTintGetter world, BlockState state, BlockPos pos, VertexConsumer vertexConsumer, PoseStack.Pose matrixEntry, BakedQuad quad, float brightness0, float brightness1, float brightness2, float brightness3, int light0, int light1, int light2, int light3, int overlay);

    @Invoker("renderQuadsSmooth")
    void callRenderQuadsSmooth(BlockAndTintGetter world, BlockState state, BlockPos pos, PoseStack matrices, VertexConsumer vertexConsumer, List<BakedQuad> quads, float[] box, BitSet flags, ModelBlockRenderer.AmbientOcclusionFace ambientOcclusionCalculator, int overlay );
}
