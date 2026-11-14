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
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.transport.block.fluid_transport.FluidBufferBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidBufferBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

@OnlyIn(Dist.CLIENT)
public class FluidBufferRenderer implements BlockEntityRenderer<FluidBufferBlockEntity>
{
    public FluidBufferRenderer(BlockEntityRendererProvider.Context context)
    {
    }

    @Override
    public void render(FluidBufferBlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        matrices.pushPose();

        matrices.translate(0.5, 0.5, 0.5);
        Direction facing = blockEntity.getBlockState().getValue(FluidBufferBlock.FACING);
        matrices.mulPose(Axis.YP.rotationDegrees(facing.getAxis() == Direction.Axis.Z ? 90 : 0));
        matrices.mulPose(Axis.ZP.rotationDegrees(facing.getAxis() == Direction.Axis.Z ? 90 : 0));
        matrices.mulPose(Axis.ZP.rotationDegrees(facing.getAxis() == Direction.Axis.X ? 90 : 0));
        matrices.translate(-0.5, -0.5, -0.5);

        WritableSingleFluidStorage buffer = blockEntity.getBuffer(null);
        FluidVariant fluid = blockEntity.getBuffer(null).getResource();
        buffer.renderLevel = Mth.lerp(0.1f, buffer.renderLevel,(buffer.getAmount()) / (float) buffer.getCapacity());
        BERenderUtils.renderFluidCuboid(vertexConsumers, matrices, fluid, 0.1f, 0.93f, 0.3f, buffer.renderLevel, light);

        matrices.popPose();
    }
}
