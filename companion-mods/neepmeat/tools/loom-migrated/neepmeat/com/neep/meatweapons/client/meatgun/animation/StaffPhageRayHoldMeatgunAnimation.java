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
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.meatgun.module.MeatgunModules;
import com.neep.meatweapons.meatgun.module.ShortPhageRayModule;
import com.neep.neepmeat.util.Easing;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class StaffPhageRayHoldMeatgunAnimation extends AnimatedAction<MeatgunComponent, StaffPhageRayHoldMeatgunAnimation> implements MeatgunAnimation
{
    private boolean started = false;

    private final Sequence<StaffPhageRayHoldMeatgunAnimation> down = new Sequence<>()
    {
        @Override
        public void tick(StaffPhageRayHoldMeatgunAnimation parent, MeatgunComponent component, int counter)
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
            float rx = -80 * ease;
            float rz = 20 * ease;

            StaffIdleMeatgunAnimation.apply(matrices, rx, 0, rz, leftHand);
            matrices.translate(0, 0.4 * ease, 0.7 * ease);
            return false;
        }
    };

    private final Sequence<StaffPhageRayHoldMeatgunAnimation> up = new Sequence<>()
    {
        @Override
        public void tick(StaffPhageRayHoldMeatgunAnimation parent, MeatgunComponent component, int counter)
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
            float ease = Math.max(0, 1 - (float) Easing.easeInCubic(delta));
            float rx = -80 * ease;
            float rz = 20 * ease;

            StaffIdleMeatgunAnimation.apply(matrices, rx, 0, rz, leftHand);
            matrices.translate(0, 0.4 * ease, 0.7 * ease);
            return false;
        }
    };

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
