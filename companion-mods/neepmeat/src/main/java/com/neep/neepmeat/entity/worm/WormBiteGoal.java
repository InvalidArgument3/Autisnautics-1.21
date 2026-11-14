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
import com.neep.neepmeat.util.NMMaths;
import java.util.EnumSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class WormBiteGoal extends AnimatedWormGoal<WormBiteGoal>
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "bite");

    protected final WormEntity parent;
    protected final float range = 16;

    protected final Sequence<WormBiteGoal> orientSequence;
    protected final Sequence<WormBiteGoal> attackSequence;
    protected final Sequence<WormBiteGoal> returnSequence;

    public WormBiteGoal(WormEntity entity)
    {
        this.parent = entity;
        this.setFlags(EnumSet.of(Flag.MOVE));

        orientSequence = (action, counter) ->
        {
            // If complete, start lunging. If there is no target, return to neutral.
            LivingEntity target = parent.getTarget();
            if (counter > 20)
            {
                action.setSequence(action.attackSequence);
                return;
            }
            if (target == null)
            {
                action.setSequence(action.returnSequence);
                return;
            }

            Vec3 neutralHead = parent.getNeutralHeadPos();
            Vec3 neutralToTarget = target.position().subtract(neutralHead);
            Vec3 baseToTarget = target.position().subtract(parent.position()).normalize();

            // Orient the entity towards the target
            Vec2 v1 = NMMaths.flattenY(baseToTarget);
            float angle = (float) Math.toDegrees(NMMaths.getAngle(v1));
            parent.setYRot(angle);

            // Determine the head position
            double targetRange = Math.min(7, baseToTarget.length());
            Vec3 headPos = neutralHead.add(baseToTarget.scale(targetRange));

            // Reposition head
            parent.lerpHeadPos(counter, 20, headPos);
            Vec2 pitchYaw = NMMaths.rectToPol(neutralToTarget);
            parent.lerpHeadAngles(counter, 20, pitchYaw.x, pitchYaw.y);

        };

        attackSequence = (action, counter) ->
        {
            if (counter > 20)
            {
                action.setSequence(action.returnSequence);
                return;
            }
            LivingEntity target = parent.getTarget();
            if (target == null)
            {
                action.setSequence(action.returnSequence);
                return;
            }

            Vec3 neutralHead = parent.getNeutralHeadPos();
            Vec3 neutralToTarget = target.position().subtract(neutralHead);

            // Place the head some distance along the vector from the neutral position to the entity.
            double targetRange = Math.min(range, neutralToTarget.length());
            Vec3 headPos = neutralHead.add(neutralToTarget.normalize().scale(targetRange));

            // Reposition head
            parent.lerpHeadPos(counter, 20, headPos, Easing::easeInBack);
            Vec2 pitchYaw = NMMaths.rectToPol(neutralToTarget);
            parent.lerpHeadAngles(counter, 20, pitchYaw.x, pitchYaw.y);
        };

        returnSequence = (action, counter) ->
        {
            if (counter > 20)
            {
                action.markFinished();
                return;
            }

            parent.returnHeadToNeutral(counter, 20);
        };
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
        setSequence(orientSequence);
        finished = false;
    }
}
