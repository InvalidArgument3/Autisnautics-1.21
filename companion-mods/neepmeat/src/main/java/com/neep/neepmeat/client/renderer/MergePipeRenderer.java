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
import com.neep.neepmeat.transport.block.item_transport.entity.MergePipeBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

@OnlyIn(Dist.CLIENT)
public class MergePipeRenderer<T extends MergePipeBlockEntity> extends ItemPipeRenderer<T>
{

    public MergePipeRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void render(T be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        super.render(be, tickDelta, matrices, vertexConsumers, light, overlay);
    }
}
