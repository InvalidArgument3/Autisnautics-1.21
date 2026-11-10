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

package com.neep.neepmeat.machine.trough;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.machine.crucible.CrucibleRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

@Environment(value = Dist.CLIENT)
@SuppressWarnings("UnstableApiUsage")
public class TroughRenderer implements BlockEntityRenderer<TroughBlockEntity>
{
    public TroughRenderer(BlockEntityRendererProvider.Context ctx)
    {

    }

    @Override
    public void render(TroughBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        WritableSingleFluidStorage storage = be.getStorage(null);
        storage.renderLevel = (float) Mth.lerp(0.1, storage.renderLevel, storage.amount / (float) storage.getCapacity());
        CrucibleRenderer.renderSurface(vertexConsumers, matrices, be.getStorage(null).getResource(), light, overlay, 1 / 16f, 9 / 16f, 1 / 16f, storage.renderLevel);
    }
}
