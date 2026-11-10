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

package com.neep.neepmeat.machine.synthesiser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.client.renderer.BERenderUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

@Environment(value = Dist.CLIENT)
public class SynthesiserRenderer implements BlockEntityRenderer<SynthesiserBlockEntity>
{
    public SynthesiserRenderer(BlockEntityRendererProvider.Context ctx)
    {

    }

    @Override
    public void render(SynthesiserBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
//        float displacement = be.maxProgress != 0 ? MathHelper.lerp(be.progress / be.maxProgress, SynthesiserBlockEntity.MAX_DISPLACEMENT, SynthesiserBlockEntity.MIN_DISPLACEMENT)
//            : SynthesiserBlockEntity.MAX_DISPLACEMENT;
        be.clientDisplacement = Mth.lerp(0.1f, be.clientDisplacement, be.getClientDisplacement());
        matrices.translate(0, be.clientDisplacement, 0);
        BERenderUtils.renderModel(NMExtraModels.SYNTHESISER_PLUNGER, matrices, be.getLevel(), be.getBlockPos(), be.getBlockState(), vertexConsumers);
    }
}
