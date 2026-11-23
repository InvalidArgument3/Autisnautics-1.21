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
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.transport.block.item_transport.machine.EjectorBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.machine.ItemPumpBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

@OnlyIn(Dist.CLIENT)
public class EjectorRenderer implements BlockEntityRenderer<EjectorBlockEntity>
{
    public EjectorRenderer(BlockEntityRendererProvider.Context context)
    {

    }

    @Override
    public void render(EjectorBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        ModelManager manager = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getModelManager();
        BakedModel shuttle = BakedModelManagerHelper.getModel(manager, NMExtraModels.ITEM_PUMP_SHUTTLE);
        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();

        Direction facing = be.getBlockState().getValue(ItemPumpBlock.FACING);
        var vec = be.getBlockState().getValue(ItemPumpBlock.FACING).step();

        matrices.pushPose();

        matrices.translate(0.5, 0.5, 0.5);
//        float offset = (float) Math.sin((world.getTime() + tickDelta) / 10);

        be.offset = (float) Mth.lerp(0.3, be.offset, be.shuttle > 0 ? (float) 0.2 : 0);

        vec.mul((float) be.offset, (float) be.offset, (float) be.offset);
        matrices.translate(vec.x, vec.y, vec.z);

        matrices.mulPose(Axis.YN.rotationDegrees(facing.toYRot()));
        if (facing == Direction.DOWN)
        {
            matrices.mulPose(Axis.YP.rotationDegrees(90));
            matrices.mulPose(Axis.XN.rotationDegrees(-90));
        }
        else if (facing == Direction.UP)
        {
            matrices.mulPose(Axis.YP.rotationDegrees(90));
            matrices.mulPose(Axis.XN.rotationDegrees(90));
        }

        matrices.translate(-0.5, -0.5, -0.5);
        renderer.tesselateBlock(
                be.getLevel(),
                shuttle,
                be.getBlockState(),
                be.getBlockPos(),
                matrices,
                vertexConsumers.getBuffer(RenderType.cutout()),
                true,
                RandomSource.create(),
                0,
                0
        );

        matrices.popPose();
    }
}
