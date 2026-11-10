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

package com.neep.meatweapons.client.meatgun.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.module.HalberdModule;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.meatgun.module.MeatgunModules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class HalberdChargeAnimation extends AnimatedAction<MeatgunComponent, HalberdChargeAnimation> implements MeatgunAnimation
{
    private boolean started = false;

    private final Sequence<HalberdChargeAnimation> up = new Sequence<>()
    {
        @Override
        public void tick(HalberdChargeAnimation parent, MeatgunComponent component, int counter)
        {
            if (!Minecraft.getInstance().player.isSprinting())
                markFinished();

//            @Nullable MeatgunModule module = RootModuleHolder.findRecursive(component.getRootHolder().root, MeatgunModules.HALBERD);
            @Nullable MeatgunModule module = component.getRootHolder().moduleCache().findModule(MeatgunModules.HALBERD);
            if (module instanceof HalberdModule halberd && halberd.triggerHeld())
//            if (module instanceof HalberdModule halberd)
            {
                started = true;
            }
            else if (started)
            {
                markFinished();
            }
        }

        @Override
        public boolean applyRender(PoseStack matrices, int counter, float tickDelta, boolean leftHand)
        {
            // Let's use trial and error to find the shoulder!
            matrices.translate(9 / 16f, -2 / 16f, 1.5);
//            float r = MathHelper.sin(AnimationTickHolder.getRenderTime() / 5);
//            Vector3f origin = matrices.peek().getPositionMatrix().getTranslation(new Vector3f());
//            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(AnimationTickHolder.getRenderTime() * 2));
            float delta = Math.min(1, (counter + tickDelta) / 10);

            float zOff = delta * 4 / 16f;
            float yOff = delta * 6 / 16f;
            matrices.translate(leftHand ? -3 / 16f : 0, -4 / 16f + yOff, zOff);
            matrices.mulPose(Axis.ZP.rotationDegrees(180));
            matrices.mulPose(Axis.XP.rotationDegrees(-90));
//            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
            matrices.translate(-9 / 16f, 2 / 16f, -1.5);
            return true;
        }
    };

    @Override
    public void start(@Nullable FriendlyByteBuf buf)
    {
        super.start(buf);
        started = false;
        setSequence(up);
    }

    @Override
    public boolean canStop(MeatgunAnimation replace)
    {
        return false;
    }
}
