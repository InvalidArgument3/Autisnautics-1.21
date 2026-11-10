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

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.neepmeat.item.AnimatedSword;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@Environment(value = Dist.CLIENT)
public class SwordRenderer<T extends AnimatedSword> extends GeoItemRenderer<T>
{
    public SwordRenderer(GeoModel<T> modelProvider)
    {
        super(modelProvider);
    }

    public void render(T animatable, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn,
                       ItemStack itemStack, ItemDisplayContext mode)
    {
//        this.currentItemStack = itemStack;
//        AnimationEvent<AnimatedSword> itemEvent = new AnimationEvent<>(animatable, 0, 0,
//                MinecraftClient.getInstance().getTickDelta(), false, Collections.singletonList(itemStack));
//
//        stack.push();
//        stack.translate(0.5, 0.5, 0.5);
//
//        MinecraftClient.getInstance().getTextureManager().bindTexture(getTextureLocation(animatable));
//        GeoModel model = modelProvider.getModel(modelProvider.getModelResource(animatable));
//        Color renderColor = getRenderColor(animatable, 0, stack, bufferIn, null, packedLightIn);
//        RenderLayer renderType = getRenderType(animatable, 0, stack, bufferIn, null, packedLightIn,
//                getTextureLocation(animatable));
//        render(model, animatable, 0, renderType, stack, bufferIn, null, packedLightIn, OverlayTexture.DEFAULT_UV,
//                (float) renderColor.getRed() / 255f, (float) renderColor.getGreen() / 255f,
//                (float) renderColor.getBlue() / 255f, (float) renderColor.getAlpha() / 255);
//        stack.pop();

//        this.currentItemStack = itemStack;
//
//        AnimationEvent<SlasherItem> itemEvent = new AnimationEvent<>(animatable, 0, 0,
//                MinecraftClient.getInstance().getTickDelta(), false, Collections.singletonList(itemStack));
//
//        if (mode.equals(ModelTransformation.Mode.GUI))
//        {
////            modelProvider.setLivingAnimations(animatable, this.getUniqueID(animatable), itemEvent);
//            return;
//        }
//
//        stack.push();
//        stack.translate(0.5, 0.5, 0.5);
//        GeoModel model = modelProvider.getModel(modelProvider.getModelLocation(animatable));
//        Color renderColor = getRenderColor(animatable, 0, stack, bufferIn, null, packedLightIn);
//        RenderLayer renderType = getRenderType(animatable, 0, stack, bufferIn, null, packedLightIn,
//                getTextureLocation(animatable));
//
//        render(model, animatable, 0, renderType, stack, bufferIn, null, packedLightIn, OverlayTexture.DEFAULT_UV,
//                (float) renderColor.getRed() / 255f, (float) renderColor.getGreen() / 255f,
//                (float) renderColor.getBlue() / 255f, (float) renderColor.getAlpha() / 255);
//        stack.pop();
    }
}
