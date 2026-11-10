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

package com.neep.meatweapons.client.renderer.meatgun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatweapons.client.MWExtraModels;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.meatgun.module.BatteryModule;
import com.neep.meatweapons.meatgun.module.SpeedModifierModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

// AVeryVeryLongName
public class PhageRaySpeedModifierModuleRenderer implements MeatgunModuleRenderer<SpeedModifierModule>
{
    private final ItemRenderer itemRenderer;

    public PhageRaySpeedModifierModuleRenderer(Minecraft client)
    {
        this.itemRenderer = client.getItemRenderer();
    }

    @Override
    public void render(ItemStack stack, MeatgunComponent component, SpeedModifierModule module, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, long time, float tickDelta, int light, int overlay)
    {
        BakedModel base = itemRenderer.getItemModelShaper().getModelManager().getModel(MWExtraModels.PHAGE_RAY_SPEED_MODIFIER);
        renderItem(stack, mode, false, matrices, vertexConsumers, light, overlay, base);
    }
}
