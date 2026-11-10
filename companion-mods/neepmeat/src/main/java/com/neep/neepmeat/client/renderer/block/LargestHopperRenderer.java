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

package com.neep.neepmeat.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.neepmeat.machine.live_machine.block.entity.LargestHopperBlockEntity;
import java.util.Random;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LargestHopperRenderer implements BlockEntityRenderer<LargestHopperBlockEntity>
{
    private final ItemRenderer itemRenderer;
    private final Random random = new Random();

    public LargestHopperRenderer(BlockEntityRendererProvider.Context context)
    {
        this.itemRenderer = context.getItemRenderer();
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
        float v;
        for (int u = 0; u < k; ++u)
        {
            matrices.pushPose();
            if (u > 0)
            {
                if (depth)
                {
                    s = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    t = (random.nextFloat() * 2.0f) * 0.15f;
                    v = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    matrices.translate(s, t, v);
                } else
                {
                    s = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    t = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    matrices.translate(s, t, 0.0);
                }
            }
            matrices.translate(0, 0, 0.25);
            matrices.mulPose(Axis.XP.rotationDegrees(depth ? 0 : -20));
            matrices.translate(0, 0, -0.25);
            itemRenderer.render(stack, ItemDisplayContext.GROUND, false, matrices, vertices, light, OverlayTexture.NO_OVERLAY, bakedModel);
            matrices.popPose();
            if (depth)
                continue;
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

    @Override
    public void render(LargestHopperBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        float[] xOffsets = {-0.45f, 0.45f, 0.45f, -0.45f};
        float[] zOffsets = {-0.45f, 0.45f, -0.45f, 0.45f};

        float yOffset = 0.25f;
        matrices.pushPose();
        matrices.translate(0.5, yOffset, 0.5);
        var slots = be.getInventory().getItems();
        for (int i = 0; i < slots.size(); ++i)
        {
            ItemStack stack = slots.get(i);

            matrices.pushPose();

            matrices.translate(xOffsets[i], 0, zOffsets[i]);

            int j = stack.isEmpty() ? 187 : Item.getId(stack.getItem()) + stack.getDamageValue();
            this.random.setSeed(j);

            matrices.translate(
                    ((random.nextFloat() * 2) - 1) * 0.15,
                    ((random.nextFloat() * 2) - 1) * 0.15,
                    ((random.nextFloat() * 2) - 1) * 0.15);
            matrices.scale(2.4f, 2.4f, 2.4f);
            renderItems(stack, matrices, vertexConsumers, itemRenderer, be.getLevel(), random, light);

            matrices.popPose();
        }
        matrices.popPose();
    }
}
