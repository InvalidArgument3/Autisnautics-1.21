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
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.client.NeepMeatClient;
import com.neep.neepmeat.client.model.GlassTankModel;
import com.neep.neepmeat.transport.block.fluid_transport.machine.GlassTankBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(value= EnvType.CLIENT)
public class GlassTankRenderer implements BlockEntityRenderer<GlassTankBlockEntity>
{
    private static ItemStack stack = new ItemStack(Items.JUKEBOX, 1);
    Model model;

    public GlassTankRenderer(BlockEntityRendererProvider.Context context)
    {
        model = new GlassTankModel(context.bakeLayer(NeepMeatClient.MODEL_GLASS_TANK_LAYER));
    }

    @Override
    public void render(GlassTankBlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        matrices.pushPose();

        WritableSingleFluidStorage buffer = blockEntity.getStorage(null);
        float scale = ((float) buffer.getAmount()) / ((float) buffer.getCapacity());
        buffer.renderLevel = Mth.lerp(0.1f, buffer.renderLevel,(buffer.getAmount()) / (float) buffer.getCapacity());
        FluidVariant fluid = blockEntity.getStorage(null).getResource();
//        renderFluidCuboid(vertexConsumers, matrices, fluid, 0.07f, 0.93f, 0.93f, buffer.renderLevel, light);
        BERenderUtils.renderFluidCuboid(vertexConsumers, matrices, fluid, 0.1f, 0.93f, 0.93f, buffer.renderLevel, light);

        matrices.popPose();
    }
}