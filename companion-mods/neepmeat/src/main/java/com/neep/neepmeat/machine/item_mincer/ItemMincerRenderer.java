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

package com.neep.neepmeat.machine.item_mincer;

import com.neep.neepmeat.api.storage.WritableStackStorage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.Random;

import static com.neep.neepmeat.machine.grinder.CrusherRenderer.renderItems;

import com.mojang.blaze3d.vertex.PoseStack;

@OnlyIn(Dist.CLIENT)
public class ItemMincerRenderer implements BlockEntityRenderer<ItemMincerBlockEntity>
{
    private final ItemRenderer itemRenderer;
    private final Random random = new Random();

    public ItemMincerRenderer(BlockEntityRendererProvider.Context ctx)
    {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(ItemMincerBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        matrices.pushPose();
        matrices.translate(0.5, 13 / 16f, 0.5);
        WritableStackStorage input = be.storage.inputStorage;
        ItemStack stack = input.getAsStack();

        int j = stack.isEmpty() ? 187 : Item.getId(stack.getItem()) + stack.getDamageValue();
        this.random.setSeed(j);

        renderItems(stack, matrices, vertexConsumers, itemRenderer, be.getLevel(), random, light);
        matrices.popPose();
    }
}
