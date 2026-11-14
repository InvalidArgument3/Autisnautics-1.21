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
import com.neep.neepmeat.util.NMMaths;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class ItemBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T>
{
    private final Function<T, ItemStack> stackFunction;
    private final Function<T, Float> offsetFunction;

    public ItemBlockEntityRenderer(BlockEntityRendererProvider.Context ctx, Function<T, ItemStack> function, Function<T, Float> offset)
    {
        this.stackFunction = function;
        this.offsetFunction = offset;
    }

    @Override
    public int getViewDistance()
    {
        return 32;
    }

    @Override
    public void render(T entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        ItemStack stack = stackFunction.apply(entity);
        float offset = offsetFunction.apply(entity);

        if (stack.isEmpty())
            return;

        matrices.pushPose();

        matrices.translate(0.5, offset, 0.5);
        // Wrap degrees to ensure precision for long-lived worlds
        matrices.mulPose(Axis.YP.rotationDegrees((NMMaths.wrapDegrees(entity.getLevel().getGameTime())) + tickDelta));

        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, matrices, vertexConsumers, null, 0);

        matrices.popPose();
    }
}
