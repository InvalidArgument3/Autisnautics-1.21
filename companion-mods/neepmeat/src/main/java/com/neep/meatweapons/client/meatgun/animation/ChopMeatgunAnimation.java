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
import com.neep.neepmeat.util.Easing;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class ChopMeatgunAnimation extends AnimatedAction<MeatgunComponent, ChopMeatgunAnimation> implements MeatgunAnimation
{
    private final RenderAction.Sequence<ChopMeatgunAnimation> down = new Sequence<>()
    {
        @Override
        public void tick(ChopMeatgunAnimation parent, MeatgunComponent component, int counter)
        {
            if (counter >= 4)
                setSequence(up);
        }

        @Override
        public boolean applyRender(PoseStack matrices, int counter, float tickDelta, boolean leftHand)
        {
            float time = counter + tickDelta;
            float delta = time / 4;
            float ease = (float) Easing.easeInBack(delta);
            float rx = -40 * ease;
            float ry = 50 * ease;

            StaffIdleMeatgunAnimation.apply(matrices, rx, ry, 0, leftHand);
            return true;
        }
    };

    private final RenderAction.Sequence<ChopMeatgunAnimation> up = new Sequence<>()
    {
        @Override
        public void tick(ChopMeatgunAnimation parent, MeatgunComponent component, int counter)
        {
            if (counter >= 10)
                markFinished();
        }

        @Override
        public boolean applyRender(PoseStack matrices, int counter, float tickDelta, boolean leftHand)
        {
            float time = counter + tickDelta;
            float delta = time / 11;
            float ease = (float) (1 - Easing.easeInBack(delta));
            float rx = -40 * ease;
            float ry = 50 * ease;
            StaffIdleMeatgunAnimation.apply(matrices, rx, ry, 0, leftHand);
            return true;
        }
    };

    @Override
    public void start(@Nullable FriendlyByteBuf buf)
    {
        super.start(buf);
        setSequence(down);
    }

    @Override
    public boolean canStop(MeatgunAnimation replace)
    {
        return replace == this;
    }
}
