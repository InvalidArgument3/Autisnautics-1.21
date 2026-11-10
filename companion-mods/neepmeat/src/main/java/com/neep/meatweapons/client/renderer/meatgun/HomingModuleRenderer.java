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
import net.minecraft.client.resources.model.ModelResourceLocation;


import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatweapons.client.MWExtraModels;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.meatgun.module.BatteryModule;
import com.neep.meatweapons.meatgun.module.HomingModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class HomingModuleRenderer implements MeatgunModuleRenderer<HomingModule>
{
    private final ItemRenderer itemRenderer;

    public HomingModuleRenderer(Minecraft client)
    {
        this.itemRenderer = client.getItemRenderer();
    }

    @Override
    public void render(ItemStack stack, MeatgunComponent component, HomingModule module, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, long time, float tickDelta, int light, int overlay)
    {
        BakedModel base = itemRenderer.getItemModelShaper().getModelManager().getModel(ModelResourceLocation.standalone(MWExtraModels.HOMING_BRAIN));
        renderItem(stack, mode, false, matrices, vertexConsumers, light, overlay, base);
    }
}
