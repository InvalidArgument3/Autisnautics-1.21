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
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3d;

@OnlyIn(Dist.CLIENT)
public class ItemPipeRenderer<T extends ItemPipeBlockEntity> implements BlockEntityRenderer<T>
{

    public ItemPipeRenderer(BlockEntityRendererProvider.Context context)
    {
    }

    @Override
    public int getViewDistance()
    {
        // items are too small to be visible beyond this
        return 26;
    }

    @Override
    public void render(T be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();

        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);

        int count = 0;
        for (ItemInPipe item : be.getItems())
        {
            // Proctection against item rendering insanity
            if (count > 20)
                break;

            count++;

            ItemStack stack = item.toItemStack();
            matrices.pushPose();

            long diff = be.getLevel().getGameTime() - item.tickStart;
            float progress = (diff + tickDelta) * item.speed;
            item.getPosition(progress, item.clientPos);

            matrices.translate(item.clientPos.x, item.clientPos.y, item.clientPos.z);
            matrices.scale(0.4f, 0.4f, 0.4f);

            matrices.mulPose(Axis.YP.rotation((float) (Math.PI * 0.75)));
            renderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, matrices, vertexConsumers, null, 0);

            matrices.popPose();
        }

        matrices.popPose();
    }
}
