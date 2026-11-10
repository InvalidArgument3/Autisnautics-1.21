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
import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.neepmeat.block.entity.BigLeverBlockEntity;
import com.neep.neepmeat.block.redstone.BigLeverBlock;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.client.NeepMeatClient;
import com.neep.neepmeat.client.model.GlassTankModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
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
import net.minecraft.world.level.block.state.properties.AttachFace;

@OnlyIn(Dist.CLIENT)
public class BigLeverRenderer<T extends BigLeverBlockEntity> implements BlockEntityRenderer<T>
{
    Model model;

    public BigLeverRenderer(BlockEntityRendererProvider.Context context)
    {
        model = new GlassTankModel(context.bakeLayer(NeepMeatClient.MODEL_GLASS_TANK_LAYER));
    }

    @Override
    public void render(T be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        Direction facing = be.getBlockState().getValue(BaseHorFacingBlock.FACING);
        AttachFace face = be.getBlockState().getValue(BigLeverBlock.FACE);

        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.YN.rotationDegrees(facing.toYRot()));

        switch (face)
        {
            case FLOOR -> matrices.mulPose(Axis.YP.rotation((float) (Math.PI)));
            case WALL -> matrices.mulPose(Axis.XP.rotation((float) (Math.PI / 2)));
            case CEILING -> matrices.mulPose(Axis.XP.rotation((float) (Math.PI)));
        }

        matrices.translate(0, -0.2, 0);
//        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(be.getWorld().getTime() + tickDelta * 1));
        boolean switched = !be.getBlockState().getValue(BigLeverBlock.POWERED);
        float angle = 20;

        if (be.activeTicks > 0)
            angle = (float) be.tickCounter / be.activeTicks * 20;

        be.leverDelta = (float) Mth.lerp(0.1, be.leverDelta, switched ? 0 : angle);
//        be.leverDelta = switched ? 0 : angle;
        matrices.mulPose(Axis.XP.rotationDegrees(be.leverDelta));
        matrices.translate(0, 0.2, 0);
        matrices.translate(-0.5, -0.5, -0.5);

        ModelManager manager = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getModelManager();
        BakedModel handle = BakedModelManagerHelper.getModel(manager, NMExtraModels.BIG_LEVER_HANDLE);
        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        renderer.tesselateBlock(
                be.getLevel(),
                handle,
                be.getBlockState(),
                be.getBlockPos(),
                matrices,
                vertexConsumers.getBuffer(RenderType.cutout()),
                true,
                RandomSource.create(),
                0,
                0
        );

//        ItemStack stack = be.getResource().toStack((int) be.getAmount());

//        be.stackRenderDelta = MathHelper.lerp(delta, be.stackRenderDelta, be.getAmount() <= 0 ? 0.3f : 0f);
//        matrices.translate(0.5, 0.25f, 0.5);
//        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((be.getWorld().getTime() + tickDelta) * 1));


        matrices.popPose();
    }
}