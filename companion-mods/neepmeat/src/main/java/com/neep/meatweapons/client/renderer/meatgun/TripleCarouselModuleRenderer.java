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
import com.mojang.math.Axis;
import com.neep.meatweapons.client.MWExtraModels;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.meatgun.module.TripleCarouselModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TripleCarouselModuleRenderer implements MeatgunModuleRenderer<TripleCarouselModule>
{
    private final ItemRenderer itemRenderer;
    private final Minecraft client;

    public TripleCarouselModuleRenderer(Minecraft client)
    {
        this.itemRenderer = client.getItemRenderer();
        this.client = client;
    }

    @Override
    public void render(ItemStack stack, MeatgunComponent component, TripleCarouselModule module, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, long time, float tickDelta, int light, int overlay)
    {
        int selected = module.getSelected();
        float rotationThing = module.getRotation(time, tickDelta);

        float baseRotationDeg = selected * 120;
//        float baseRotationDeg = 0;
        float rotation = baseRotationDeg + rotationThing * 120;
        module.lerpAngle = Mth.rotLerp(0.6f, module.lerpAngle, rotation);

        matrices.translate(0.5, 0, 0.5);
        matrices.mulPose(Axis.ZN.rotationDegrees(rotation));
        matrices.translate(-0.5, 0, -0.5);
        BakedModel base = itemRenderer.getItemModelShaper().getModelManager().getModel(ModelResourceLocation.standalone(MWExtraModels.TRIPLE_CAROUSEL));
        renderItem(stack, mode, false, matrices, vertexConsumers, light, overlay, base);
    }
}
