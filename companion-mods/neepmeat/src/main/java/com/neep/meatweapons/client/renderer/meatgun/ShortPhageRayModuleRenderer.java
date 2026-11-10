/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.meatweapons.client.renderer.meatgun;
import net.minecraft.client.resources.model.ModelResourceLocation;


import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatlib.client.RotationAxis;
import com.neep.meatweapons.client.MWExtraModels;
import com.neep.meatweapons.client.MWKeys;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.meatgun.module.ShortPhageRayModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ShortPhageRayModuleRenderer implements MeatgunModuleRenderer<ShortPhageRayModule>
{
    private final ItemRenderer itemRenderer;

    public ShortPhageRayModuleRenderer(Minecraft client)
    {
        this.itemRenderer = client.getItemRenderer();
    }

    @Override
    public void render(ItemStack stack, MeatgunComponent component, ShortPhageRayModule module, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, long time, float tickDelta, int light, int overlay)
    {
        BakedModel base = itemRenderer.getItemModelShaper().getModelManager().getModel(ModelResourceLocation.standalone(MWExtraModels.SHORT_PHAGE_RAY));
        renderItem(stack, mode, false, matrices, vertexConsumers, light, overlay, base);

        boolean using = MWKeys.trigger1Held || MWKeys.trigger2Held;
        float panelOffset = using ? 2 / 16f : 0;
        module.lerpPanelOffset = Mth.lerp(0.1f, module.lerpPanelOffset, panelOffset);

        BakedModel panel = itemRenderer.getItemModelShaper().getModelManager().getModel(ModelResourceLocation.standalone(MWExtraModels.SHORT_PHAGE_RAY_PANEL));
        matrices.pushPose();
        matrices.translate(0, module.lerpPanelOffset, 0);
        renderItem(stack, mode, false, matrices, vertexConsumers, light, overlay, panel);
        matrices.popPose();

        matrices.pushPose();
        matrices.translate(0.5, -0 / 16f, 0);
        matrices.mulPose(RotationAxis.POSITIVE_Z.rotationDegrees(180));
        matrices.translate(-0.5, 0 / 16f, 0);
        matrices.translate(0, module.lerpPanelOffset, 0);
        renderItem(stack, mode, false, matrices, vertexConsumers, light, overlay, panel);
        matrices.popPose();
    }
}
