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

package com.neep.neepmeat.entity.worm;

import net.minecraft.world.entity.ai.goal.Goal;

public abstract class AnimatedWormGoal<T extends WormAction> extends Goal implements WormAction
{
    protected final Class<T> clazz;
    protected boolean finished;
    protected Sequence<T> sequence;
    protected int counter;

    public AnimatedWormGoal()
    {
        this.clazz = (Class<T>) getClass();
    }

    @Override
    public void start()
    {
        finished = false;
    }

    @Override
    public void tick()
    {
        if (sequence != null) sequence.tick(clazz.cast(this), counter);

        ++counter;
    }

    @Override
    public boolean requiresUpdateEveryTick()
    {
        return true;
    }

    @Override
    public boolean canContinueToUse()
    {
        return !finished;
    }

    public void setSequence(Sequence<T> sequence)
    {
        // Reset the tick counter and replace the current sequence
        this.sequence = sequence;
        counter = 0;
    }

    public void markFinished()
    {
        this.finished = true;
    }
}