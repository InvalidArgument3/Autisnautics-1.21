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
import com.mojang.math.Axis;
import com.neep.neepmeat.block.entity.machine.AgitatorBlockEntity;
import com.neep.neepmeat.block.machine.AgitatorBlock;
import com.neep.neepmeat.client.NMExtraModels;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

@OnlyIn(Dist.CLIENT)
public class AgitatorRenderer implements BlockEntityRenderer<AgitatorBlockEntity>
{
    public AgitatorRenderer(BlockEntityRendererProvider.Context context)
    {
    }

    @Override
    public void render(AgitatorBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        matrices.pushPose();


        matrices.pushPose();
        Direction facing = be.getBlockState().getValue(AgitatorBlock.FACING);

//        BERenderUtils.rotateFacing(facing, matrices);
        matrices.translate(0.5, 0.5, 0.5);
        if (facing == Direction.DOWN)
            matrices.mulPose(Axis.XP.rotationDegrees(180));

        matrices.mulPose(Axis.YP.rotation((be.getLevel().getGameTime() + tickDelta) / 10f));
        matrices.translate(-0.5, -0.5, -0.5);

        matrices.translate(0, 1, 0);
        BERenderUtils.renderModel(NMExtraModels.AGITATOR_BLADES, matrices, be.getLevel(), be.getBlockPos(), be.getBlockState(), vertexConsumers);

        matrices.popPose();


        matrices.popPose();
    }
}
