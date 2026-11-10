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

package com.neep.neepmeat.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.neepbus.client.input.InputInterceptorsClient;
import com.neep.neepmeat.api.item.OverrideSwingItem;
import com.neep.neepmeat.client.plc.PLCHudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HeldItemRendererMixin
{
    @Shadow private float equipProgressMainHand;

    @Shadow private ItemStack mainHand;

    @Shadow private ItemStack offHand;

    @Shadow private float equipProgressOffHand;

    @Inject(method = "renderFirstPersonItem", at = @At(value = "TAIL"))
    public void renderItemHead(AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci)
    {
        if (stack.getItem() instanceof OverrideSwingItem item && stack.equals(player.getMainHandItem()))
        {
            if (player.swinging)
            {
                player.swinging = item.onSwing(stack, player);
            }
        }
    }

    @Inject(method = "updateHeldItems", at = @At(value = "TAIL"))
    public void render(CallbackInfo ci)
    {
        LocalPlayer clientPlayer = Minecraft.getInstance().player;
        ItemStack mainHandStack = clientPlayer.getMainHandItem();
        ItemStack offHandStack = clientPlayer.getOffhandItem();

        if (mainHandStack.getItem() instanceof OverrideSwingItem overrideSwing && overrideSwing.setEquipProgess()
                && ItemStack.isSameItem(mainHand, mainHandStack))
        {
            this.equipProgressMainHand = 1;
            this.mainHand = mainHandStack;
        }

        if (offHandStack.getItem() instanceof OverrideSwingItem overrideSwing && overrideSwing.setEquipProgess()
                && ItemStack.isSameItem(offHand, offHandStack))
        {
            this.equipProgressOffHand = 1;
            this.offHand = offHandStack;
        }

    }

    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At(value = "HEAD"), cancellable = true)
    public void onRenderItem(float tickDelta, PoseStack matrices, MultiBufferSource.BufferSource vertexConsumers, LocalPlayer player, int light, CallbackInfo ci)
    {
        if (PLCHudRenderer.active()
                || (InputInterceptorsClient.currentHandler() != null && InputInterceptorsClient.currentHandler().hideHud()))
            ci.cancel();
    }
}
