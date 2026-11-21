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

package com.neep.neepmeat.machine.grinder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.client.renderer.BERenderUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class CrusherRenderer implements BlockEntityRenderer<CrusherBlockEntity>
{
    private final Random random = new Random();
    private final ItemRenderer itemRenderer;

    public CrusherRenderer(BlockEntityRendererProvider.Context ctx)
    {
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(CrusherBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        if (!Minecraft.getInstance().isPaused())
            be.sinAngle += Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true) * 4 * be.progressIncrement();

        Direction facing = be.getBlockState().getValue(CrusherBlock.FACING);

        matrices.pushPose();
        BERenderUtils.rotateFacing(facing, matrices);
        float yOffset = 11 / 16f;
        if (be.currentRecipe != null && be.progressIncrement() > 0)
        {
            yOffset += (float) ((Math.sin(be.sinAngle)) * 0.02);
        }

        matrices.pushPose();
        matrices.translate(0.5, yOffset, 0.5);
        WritableStackStorage input = be.getStorage().getInputStorage();
        ItemStack stack = input.getAsStack();

        int j = stack.isEmpty() ? 187 : Item.getId(stack.getItem()) + stack.getDamageValue();
        this.random.setSeed(j);

        matrices.mulPose(Axis.XP.rotationDegrees(-45));
        matrices.translate(-0.5, -0.5, -0.5);
        BERenderUtils.rotateFacing(facing, matrices);
        matrices.translate(0.5, 0.5, 0.5);
        renderItems(stack, matrices, vertexConsumers, itemRenderer, be.getLevel(), random, light);
        matrices.popPose();
        matrices.popPose();
    }

    public static void renderItems(ItemStack stack, PoseStack matrices, MultiBufferSource vertices, ItemRenderer itemRenderer, Level world, Random random, int light)
    {
        matrices.pushPose();
        int k = getRenderedAmount(stack);
        BakedModel bakedModel = itemRenderer.getModel(stack, world, null, 0);
        boolean depth = bakedModel.isGui3d();
        float sX = bakedModel.getTransforms().ground.scale.x;
        float sY = bakedModel.getTransforms().ground.scale.y;
        float sZ = bakedModel.getTransforms().ground.scale.z;

        // Rotate by 1 degree to prevent axis fighting with nearby block models
        matrices.mulPose(Axis.YP.rotationDegrees(1));


        float t;
        float s;
        for (int u = 0; u < k; ++u)
        {
            matrices.pushPose();
            if (u > 0)
            {
                if (depth)
                {
                    s = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    t = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    float v = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    matrices.translate(s, t, v);
                } else
                {
                    s = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    t = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    matrices.translate(s, t, 0.0);
                }
            }
            itemRenderer.render(stack, ItemDisplayContext.GROUND, false, matrices, vertices, light, OverlayTexture.NO_OVERLAY, bakedModel);
            matrices.popPose();
            if (depth) continue;
            matrices.translate(0.0f * sX, 0.0f * sY, 0.09375f * sZ);
        }
        matrices.popPose();
    }

    public static int getRenderedAmount(ItemStack stack)
    {
        int i = 1;
        if (stack.getCount() > 48)
        {
            i = 5;
        }
        else if (stack.getCount() > 32)
        {
            i = 4;
        }
        else if (stack.getCount() > 16)
        {
            i = 3;
        }
        else if (stack.getCount() > 1)
        {
            i = 2;
        }
        return i;
    }

}
