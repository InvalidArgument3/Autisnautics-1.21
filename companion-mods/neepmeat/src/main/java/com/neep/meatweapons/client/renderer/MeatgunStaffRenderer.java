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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.meatweapons.client.meatgun.RecoilManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MeatgunStaffRenderer extends MeatgunPistolRenderer
{
    @Override
    protected void renderInner(ItemStack stack, AbstractClientPlayer player, PlayerRenderer playerEntityRenderer, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vcp, boolean showArm, boolean mainHand, boolean leftHanded, float tickDelta, int light, int overlay)
    {
        super.renderInner(stack, player, playerEntityRenderer, mode, matrices, vcp, showArm, mainHand, leftHanded, tickDelta, light, overlay);
    }

    @Override
    protected void renderArms(PoseStack matrices, ItemDisplayContext mode, AbstractClientPlayer player, PlayerRenderer playerEntityRenderer, MultiBufferSource vcp, int light, boolean mainHand, boolean leftHanded)
    {
        super.renderArms(matrices, mode, player, playerEntityRenderer, vcp, light, mainHand, leftHanded);
    }

    @Override
    public void transformRecoil(PoseStack matrices, RecoilManager recoil, boolean leftHand)
    {
        matrices.translate(0, 0, recoil.horAmount);
        if (recoil.amount != 0)
        {
            matrices.translate(0, 0, 1.4);
            matrices.mulPose(Axis.XP.rotationDegrees(recoil.amount));
            matrices.translate(0, 0, -1.4);
        }
    }
}
