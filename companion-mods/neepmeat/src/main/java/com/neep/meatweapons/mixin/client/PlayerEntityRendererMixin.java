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

import com.neep.meatweapons.client.renderer.ArmPoseHelper;
import com.neep.meatweapons.item.AssaultDrillItem;
import com.neep.meatweapons.item.BaseGunItem;
import com.neep.meatweapons.item.WeakTwoHanded;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class PlayerEntityRendererMixin
{
    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private static void getArmPose(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir)
    {
        Item item = player.getItemInHand(hand).getItem();
        if (item instanceof WeakTwoHanded weakTwoHanded)
        {
            cir.setReturnValue(ArmPoseHelper.getPose(weakTwoHanded.getThirdPersonArmPose(player, player.getItemInHand(hand), hand)));
        }
        else if (item instanceof BaseGunItem || item instanceof AssaultDrillItem)
        {
            cir.setReturnValue(HumanoidModel.ArmPose.BOW_AND_ARROW);
        }
    }
}
