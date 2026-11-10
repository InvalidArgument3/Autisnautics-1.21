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

package com.neep.neepmeat.client.meatgun.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.meatweapons.client.meatgun.animation.AnimatedAction;
import com.neep.meatweapons.client.meatgun.animation.MeatgunAnimation;
import com.neep.meatweapons.client.meatgun.animation.StaffIdleMeatgunAnimation;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.meatgun.module.MeatgunModules;
import com.neep.meatweapons.meatgun.module.ShortPhageRayModule;
import com.neep.neepmeat.util.Easing;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class DrillPhageRayHoldMeatgunAnimation extends AnimatedAction<MeatgunComponent, DrillPhageRayHoldMeatgunAnimation> implements MeatgunAnimation
{
    private boolean started = false;

    private final Sequence<DrillPhageRayHoldMeatgunAnimation> down = new Sequence<>()
    {
        @Override
        public void tick(DrillPhageRayHoldMeatgunAnimation parent, MeatgunComponent component, int counter)
        {
            @Nullable MeatgunModule module = component.getRootHolder().moduleCache().findModule(MeatgunModules.SHORT_PHAGE_RAY);
            if (module instanceof ShortPhageRayModule phageRay)
            {
                if (phageRay.triggerHeld())
                    started = true;
                else if (started)
                    setSequence(up);
            }
            else
            {
                markFinished();
            }
        }

        @Override
        public boolean applyRender(PoseStack matrices, int counter, float tickDelta, boolean leftHand)
        {
            float time = counter + tickDelta;
            float delta = time / 2;
            float ease = Math.min(1, (float) Easing.easeInCubic(delta));
            float rx = 3 * ease;

            applyIdle(matrices, rx, 0, 0, leftHand);
            return false;
        }
    };

    private final Sequence<DrillPhageRayHoldMeatgunAnimation> up = new Sequence<>()
    {
        @Override
        public void tick(DrillPhageRayHoldMeatgunAnimation parent, MeatgunComponent component, int counter)
        {
            @Nullable MeatgunModule module = component.getRootHolder().moduleCache().findModule(MeatgunModules.SHORT_PHAGE_RAY);
            if (module instanceof ShortPhageRayModule phageRay)
            {
                if (phageRay.triggerHeld())
                    started = true;
                else if (started)
                    markFinished();
            }
            else
            {
                markFinished();
            }
        }

        @Override
        public boolean applyRender(PoseStack matrices, int counter, float tickDelta, boolean leftHand)
        {
            float time = counter + tickDelta;
            float delta = time / 2;
            float ease = (float) Math.max(0f, 1f - Easing.easeInCubic(delta));
            float rx = 3 * ease;

            applyIdle(matrices, rx, 0, 0, leftHand);
            return false;
        }
    };

    public static void applyIdle(PoseStack matrices, float xRot, float yRot, float zRot, boolean leftHand)
    {
        float i = leftHand ? -1 : 1;
        float d = leftHand ? 16 / 16f : 0;
//        matrices.translate(0, -4 / 16f, 0 / 16f);
        matrices.translate(d, 0, 1.5);
        matrices.mulPose(Axis.YP.rotationDegrees(i * (yRot)));
        matrices.mulPose(Axis.XP.rotationDegrees(xRot));
        matrices.translate(-d, 0, -1.5);

        matrices.translate(d, 0, d);
        matrices.mulPose(Axis.ZP.rotationDegrees(i * (zRot)));
        matrices.translate(-d, 0, -d);
    }

    @Override
    public void start(@Nullable FriendlyByteBuf buf)
    {
        super.start(buf);
        setSequence(down);
        started = false;
    }

    @Override
    public boolean canStop(MeatgunAnimation replace)
    {
        return replace == this;
    }
}
