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
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public abstract class AnimatedAction<E, Self extends AnimatedAction<E, Self>> implements RenderAction, MeatgunAnimation
{
    protected boolean finished;
    protected Sequence<Self> sequence;
    protected int counter;

    public AnimatedAction()
    {
    }

    public void start(@Nullable FriendlyByteBuf buf)
    {
        finished = false;
    }

    @Override
    public void tick(MeatgunComponent component)
    {
        ++counter;

        if (sequence != null && !finished)
            sequence.tick((Self) this, component, counter);
    }

    @Override
    public boolean applyRender(PoseStack matrices, float tickDelta, boolean leftHand)
    {
        if (sequence != null)
            return sequence.applyRender(matrices, counter, tickDelta, leftHand);

        return true;
    }

    public void setSequence(Sequence<Self> sequence)
    {
        // Reset the tick counter and replace the current sequence
        this.sequence = sequence;
        counter = 0; // Jank
    }

    public void markFinished()
    {
        this.finished = true;
    }

    @Override
    public boolean finished()
    {
        return finished;
    }
}
