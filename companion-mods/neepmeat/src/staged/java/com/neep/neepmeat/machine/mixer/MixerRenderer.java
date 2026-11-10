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

package com.neep.neepmeat.machine.mixer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.client.renderer.BERenderUtils;
import com.neep.neepmeat.client.renderer.MultiFluidRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("UnstableApiUsage")
public class MixerRenderer implements BlockEntityRenderer<MixerBlockEntity>
{

    public MixerRenderer(BlockEntityRendererProvider.Context ctx)
    {
    }

    @Override
    public void render(MixerBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        matrices.pushPose();
        matrices.translate(0, 1 - 5f / 16f, 0);
        float progress = 0;
        float nextOutput = 0;
        if (be.getCurrentRecipe() != null && be.progressIncrement() > be.minPower)
        {
            progress = (be.progress) / be.processLength;
            nextOutput = progress * be.getCurrentRecipe().fluidOutput.maxAmount();
        }


        WritableSingleFluidStorage storage = (WritableSingleFluidStorage) be.storage.getFluidOutput();
        storage.renderLevel = Mth.lerp(0.05f, storage.renderLevel,(storage.getAmount() + nextOutput) / (float) storage.getCapacity());
        float outputEnd = storage.renderLevel;
        FluidVariant output = storage.getResource();

        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.scale(0.9f, 1, 0.9f);
        matrices.translate(-0.5, -0.5, -0.5);
        MultiFluidRenderer.renderFluidCuboid(vertexConsumers, matrices, output, 0, outputEnd, outputEnd, 1, light);

        if (be.getCurrentRecipe() != null)
        {
            FluidVariant var1 = be.storage.displayInput1;
            FluidVariant var2 = be.storage.displayInput2;

            float scale = (-Math.abs(2 * progress - 1) + 1) * 0.1f;

            MultiFluidRenderer.renderFluidCuboid(vertexConsumers, matrices, var1, outputEnd, outputEnd + scale, outputEnd + scale, 1, light);
            MultiFluidRenderer.renderFluidCuboid(vertexConsumers, matrices, var2, outputEnd + scale, outputEnd + scale * 2, outputEnd + scale * 2, 1, light);
        }
        matrices.popPose();
        matrices.popPose();

        matrices.pushPose();
        matrices.translate(0.5, 1.5, 0.5);

        float delta = Minecraft.getInstance().isPaused() ? 0 : Minecraft.getInstance().getDeltaFrameTime();

        be.bladeSpeed = Mth.lerp(0.5f, be.bladeSpeed,
                be.currentRecipe == null || be.progressIncrement() <= be.minPower ? 0 : 50f * be.progressIncrement()
        );

        be.bladeAngle = Mth.wrapDegrees(be.bladeAngle + be.bladeSpeed * delta);
        matrices.mulPose(Axis.YP.rotationDegrees(be.bladeAngle));
        matrices.translate(-0.5, -0.5, -0.5);
        BERenderUtils.renderModel(NMExtraModels.MIXER_AGITATOR_BLADES, matrices, be.getLevel(), be.getBlockPos(), be.getBlockState(), vertexConsumers);

        renderItems(be, matrices, 0, 0, outputEnd - 0.4f, 0.4f, tickDelta, vertexConsumers, overlay, light);
        matrices.popPose();
    }

    public static void renderItems(MixerBlockEntity be, PoseStack matrices, float angle, float angleOffset, float fluidHeight, float offset, float tickDelta, MultiBufferSource vertexConsumers, int overlay, int light)
    {

        WritableStackStorage view = be.getItemStorage(null);
        matrices.pushPose();
        matrices.translate(0.5, 0, 0.5);

        int n = (int) Mth.clamp(view.amount / 14f, 1, 3);

        for (int i = 0; i < n; ++i)
        {
            float bobOffset = (float) ((float) i * (2 / (float) n * Math.PI));
            float yOffset = (float) Mth.clamp(fluidHeight + 0.03f * Math.sin((be.getLevel().getGameTime() + tickDelta) / 6 + bobOffset), -0.27f, 1 - 0.4f);

            matrices.pushPose();
            matrices.mulPose(Axis.YP.rotation(angle + angleOffset));
//            matrices.multiply(Quaternion.fromEulerXyz(0, (float) (angle + angleOffset), 0));
            matrices.translate(0, yOffset, offset);
            Minecraft.getInstance().getItemRenderer()
                    .renderStatic(view.getResource().toStack((int) view.getAmount()), ItemDisplayContext.GROUND, light, overlay, matrices, vertexConsumers, null, 0);
            angleOffset += 2 * Math.PI / n;
            matrices.popPose();
        }
        matrices.popPose();
    }
}