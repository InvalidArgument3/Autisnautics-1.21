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

package com.neep.neepmeat.machine.bottler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.client.renderer.BERenderUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@OnlyIn(Dist.CLIENT)
public class BottlerRenderer implements BlockEntityRenderer<BottlerBlockEntity>
{
    public BottlerRenderer(BlockEntityRendererProvider.Context ctx)
    {

    }

    @Override
    public void render(BottlerBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        Direction facing = be.getBlockState().getValue(BottlerBlock.FACING);
        ItemStack stack = be.getItemStorage(null).getAsStack();

        matrices.pushPose();
        BERenderUtils.rotateFacing(facing, matrices);
        matrices.translate(0.5, 0.5, 0.15);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, matrices, vertexConsumers, null, 0);
        matrices.popPose();

        float timeDiff = be.getLevel().getGameTime() + tickDelta - be.getStartTime();
        float delta = timeDiff / ((float) be.getMaxProgress() / be.progressIncrement());
        float yOffset = Math.min(0, Mth.lerp(delta, -8 / 16f, 0));
        matrices.translate(0, yOffset, 0);
        BERenderUtils.renderModel(NMExtraModels.PUMP, matrices, be.getLevel(), be.getBlockPos(), be.getBlockState(), vertexConsumers);
    }
}
