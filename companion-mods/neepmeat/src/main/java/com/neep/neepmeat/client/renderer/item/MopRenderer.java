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

package com.neep.neepmeat.client.renderer.item;
import net.minecraft.client.resources.model.ModelResourceLocation;


import com.neep.meatlib.client.RotationAxis;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.client.item.ItemRenderUtil;
import com.neep.neepmeat.util.Easing;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import static com.neep.neepmeat.client.renderer.item.DrillChassisItemRenderer.renderItem;

import com.mojang.blaze3d.vertex.PoseStack;

public class MopRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer
{
    public static final MopRenderer INSTANCE = new MopRenderer(Minecraft.getInstance());

    private final Minecraft client;

    public MopRenderer(Minecraft client)
    {
        this.client = client;
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vcp, int light, int overlay)
    {
        BakedModel main = client.getItemRenderer().getItemModelShaper().getModelManager().getModel(ModelResourceLocation.standalone(NMExtraModels.MOP_MAIN));

        if (client.player != null && mode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || mode == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
        {
            boolean leftHanded = mode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            float swingProgress = client.player.getAttackAnim(client.getTimer().getRealtimeDeltaTicks());

            // Remove all the other transformations including the equip animation and display settings.
            matrices.popPose();
            matrices.popPose();
            matrices.pushPose();
            matrices.pushPose();

            ItemRenderUtil.defaultItemEquipAnimation(matrices, client, mode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND);

            ItemTransforms modelTransformation = client.getItemRenderer().getItemModelShaper().getItemModel(stack).getTransforms();
            ItemTransform transformation = modelTransformation.getTransform(mode);

            matrices.pushPose();
            transformation.apply(leftHanded, matrices);
            matrices.translate(-0.5F, -0.5F, -0.5F);

            float towardsCentre = animateSwing(swingProgress);
            double wobble = Math.sin(Minecraft.getInstance().getFrameTimeNs() * 1.7);
            matrices.translate(
                    0,
                    towardsCentre * wobble * 8 / 16f,
                    towardsCentre * -13 / 16f
            );

            matrices.mulPose(RotationAxis.POSITIVE_X.rotationDegrees(towardsCentre * 40));

            renderItem(stack, mode, matrices, vcp, light, overlay, main, false, false);

            matrices.popPose();
        }
        else
        {
            renderItem(stack, mode, matrices, vcp, light, overlay, main, false, false);
        }
    }

    private float animateSwing(float x)
    {
        return (float) (x < 0.3f ? Easing.easeOutCubic(x / 0.3f) : Easing.easeInCubic((1 - x) / 0.7f));
//        return (float) (x < 0.3f ? (x / 0.3f) : ((1 - x) / 0.7f));
    }
}
