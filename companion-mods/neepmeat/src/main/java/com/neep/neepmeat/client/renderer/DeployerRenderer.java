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
import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.machine.deployer.DeployerBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

@OnlyIn(Dist.CLIENT)
public class DeployerRenderer implements BlockEntityRenderer<DeployerBlockEntity>
{
    public DeployerRenderer(BlockEntityRendererProvider.Context context)
    {
    }

    @Override
    public void render(DeployerBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        matrices.pushPose();

        // Offset shuttle while powered
        be.shuttleOffset = (float) Mth.lerp(0.3, be.shuttleOffset, be.powered ? (float) 0.1 : 0);

        matrices.pushPose();
        Direction facing = be.getBlockState().getValue(BaseFacingBlock.FACING);
        BERenderUtils.rotateFacing(facing, matrices);
        matrices.translate(0, 0, -be.shuttleOffset);
        BERenderUtils.renderModel(NMExtraModels.DEPLOYER_SHUTTLE, matrices, be.getLevel(), be.getBlockPos(), be.getBlockState(), vertexConsumers);

//        float scale = 0.4f + be.shuttleOffset;
//        if (stack.getItem() instanceof BlockItem blockItem)
//        {
//            matrices.translate(0.5, 0.5, 0.5);
//            matrices.scale(scale, scale, scale);
//            matrices.translate(-0.5, -0.5, -0.5);
//
//            MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(blockItem.getBlock().getDefaultState(),
//                    matrices,
//                    vertexConsumers,
//                    light,
//                    overlay);
//        }
//        else
//        {
//            matrices.translate(0.5, 0.4, 0.5);
//            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, null, 0);
//        }
        matrices.popPose();


        matrices.popPose();
    }
}
