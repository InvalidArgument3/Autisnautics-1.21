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

package com.neep.meatweapons.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatweapons.client.renderer.ArmRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HeldItemRendererMixin
{
    @Inject(method = "updateHeldItems", at = @At(value = "TAIL"))
    public void render(CallbackInfo ci)
    {
        // Using this.client.player returns null in unpredictable circumstances.
//        ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
//        ItemStack itemStack = clientPlayerEntity.getMainHandStack();
//        ItemStack itemStack2 = clientPlayerEntity.getOffHandStack();
//
//        if (itemStack.getItem() instanceof BaseGunItem && ItemStack.areItemsEqualIgnoreDamage(mainHand, itemStack))
//        {
//            this.equipProgressMainHand = 1;
//            this.mainHand = itemStack;
//        }
//        if (itemStack2.getItem() instanceof BaseGunItem && ItemStack.areItemsEqualIgnoreDamage(offHand, itemStack2))
//        {
//            this.equipProgressOffHand = 1;
//            this.offHand = itemStack2;
//        }
    }

    // Causes offhand arm to render for certain weapons.
    @Inject(method = "renderFirstPersonItem", at = @At(value = "TAIL"))
    public void renderItemHead(AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci)
    {
        ArmRenderer.onRenderArm(matrices, player, item, hand, ((ItemInHandRenderer) (Object) this), equipProgress, swingProgress, vertexConsumers, light);
    }
}
