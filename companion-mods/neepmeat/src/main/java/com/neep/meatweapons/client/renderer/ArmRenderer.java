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

package com.neep.meatweapons.client.renderer;

import com.neep.meatweapons.item.meatgun.MeatgunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.meatweapons.item.WeakTwoHanded;
import com.neep.meatweapons.mixin.client.HeldItemRendererAccessor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public class ArmRenderer
{
    public static void onRenderArm(PoseStack matrices, AbstractClientPlayer player, ItemStack stack, InteractionHand hand, ItemInHandRenderer heldItemRenderer,
                                   float equipProgress, float swingProgress, MultiBufferSource vcp, int light)
    {
        boolean isAiming = player.isShiftKeyDown();
        if (stack.getItem() instanceof MeatgunItem)
        {
//            if (hand == Hand.MAIN_HAND && player.getOffHandStack().isEmpty() && !player.handSwinging && !isAiming)
//            {
//                matrices.push();
//
//                MeatgunComponent component = MWComponents.MEATGUN.get(stack);
//                RecoilManager recoil = component.getRecoil();
//
//                transformRecoil(matrices, recoil);
//
//                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(player.getMainArm() == Arm.RIGHT ? -55 : 55));
////                ((HeldItemRendererAccessor) heldItemRenderer).callRenderArmHoldingItem(matrices, vcp, light, equipProgress, swingProgress, player.getMainArm().getOpposite());
//                matrices.pop();
//            }
        }
        else if (stack.getItem() instanceof WeakTwoHanded weakTwoHanded && weakTwoHanded.displayArmFirstPerson(player.getItemInHand(hand), hand))
        {
            // Offhand will only be rendered if empty and not swinging.
            if (hand == InteractionHand.MAIN_HAND && player.getOffhandItem().isEmpty() && !player.swinging && !isAiming)
            {
                matrices.pushPose();
                // Change rotation if main hand is left.
                matrices.mulPose(Axis.YP.rotationDegrees(player.getMainArm() == HumanoidArm.RIGHT ? -55 : 55));
                ((HeldItemRendererAccessor) heldItemRenderer).callRenderArmHoldingItem(matrices, vcp, light, equipProgress, swingProgress, player.getMainArm().getOpposite());
                matrices.popPose();
            }
            if (isAiming)
            {
                matrices.pushPose();
                matrices.popPose();
            }
        }

    }
}
