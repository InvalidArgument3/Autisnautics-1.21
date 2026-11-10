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

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.util.Easing;
import java.util.EnumSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class WormFullSwingGoal extends AnimatedWormGoal<WormFullSwingGoal>
{
    public static final ResourceLocation ID = new ResourceLocation(NeepMeat.NAMESPACE, "full_swing");

    protected final WormEntity parent;

    protected float angle;
    protected float finalAngle;
    protected float radius;

    protected final Sequence<WormFullSwingGoal> orient = (action, counter) ->
    {
        if (counter > 20)
        {
            action.setSequence(action.swing);
            return;
        }
        if (action.parent.getTarget() == null)
        {
            action.setSequence(action.neutral);
        }

        action.parent.lerpHeadPos(counter, 20, getHeadPos(angle));
        action.parent.lerpHeadAngles(counter, 20, 0, angle);
    };

    protected final Sequence<WormFullSwingGoal> swing = (action, counter) ->
    {
        if (counter > 20)
        {
            action.setSequence(action.reset);
            return;
        }
        if (action.parent.getTarget() == null)
        {
            action.setSequence(action.neutral);
        }

        action.angle = (float) (-360 * Easing.easeOutBack(counter / 20f));

        apply(action.angle);

    };

    protected final Sequence<WormFullSwingGoal> reset = (action, counter) ->
    {
        apply(action.angle + 360);
        action.setSequence(action.neutral);
    };

    protected final Sequence<WormFullSwingGoal> neutral = (action, counter) ->
    {
        if (counter > 20)
        {
            action.markFinished();
            return;
        }

        action.parent.returnHeadToNeutral(counter, 20);
    };

    public WormFullSwingGoal(WormEntity entity, float radius)
    {
        this.parent = entity;
        this.angle = entity.getYRot();
        this.finalAngle = angle - 360;
        this.radius = radius;

        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse()
    {
        return parent.getTarget() != null
                && parent.getRandom().nextBoolean();
    }

    @Override
    public void start()
    {
        super.start();
        setSequence(orient);
    }

    protected Vec3 getHeadPos(float angle)
    {
        return new Vec3(
                parent.getX() + radius * Math.sin(Math.toRadians(angle)),
                parent.getY() + 5,
                parent.getZ() + radius * Math.cos(Math.toRadians(angle))
        );
    }

    protected void apply(float angle)
    {
        parent.setHeadAngles(0, angle);
        Vec3 headPos = getHeadPos(angle);
        parent.setHeadPos(headPos.x, headPos.y, headPos.z);
    }
}
