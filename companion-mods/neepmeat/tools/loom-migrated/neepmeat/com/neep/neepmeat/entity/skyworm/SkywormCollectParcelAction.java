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

package com.neep.neepmeat.entity.skyworm;

import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.util.Easing;
import net.minecraft.sounds.SoundSource;

public class SkywormCollectParcelAction extends SkywormAction<SkywormCollectParcelAction>
{
    private double initialY;
    private final SkywormAnimationTimes times;

    public SkywormCollectParcelAction(SkywormEntity mob, double initialY, double targetY, SkywormAnimationTimes times)
    {
        super(mob);
        this.initialY = initialY;
        this.times = times;
        this.targetY = targetY;
        setSequence(descend);
    }

    private final Sequence<SkywormCollectParcelAction> descend = (t, counter) ->
    {
        if (counter == 0)
            getWorld().playSound(null, getX(), targetY, getZ(), NMSounds.SKYWORM_DESCEND_SHORT, SoundSource.NEUTRAL, 2, 1);

        double newY = t.initialY + (targetY - t.initialY) * Easing.easeOutCubic(counter / (float) t.times.descend());

        mob.setPosRaw(getX(), newY, getZ());
        mob.setDeltaMovement(0, 0, 0);

        if (getY() == targetY)
        {
            var y = getY();
            targetY = t.initialY;
            t.initialY = getY();
            setSequence(t.stay);
        }
    };

    private final Sequence<SkywormCollectParcelAction> stay = (t, counter) ->
    {
        if (counter == 0)
        {
            mob.setBulge(-1);
            mob.setBulgeDirection(1);
        }

        mob.setDeltaMovement(0, 0, 0);
        if (counter >= t.times.stay())
        {
            var y = getY();
            setSequence(t.ascend);
        }
    };

    private final Sequence<SkywormCollectParcelAction> ascend = (t, counter) ->
    {
        if (counter == 0)
            getWorld().playSound(null, getX(), initialY, getZ(), NMSounds.SKYWORM_ASCEND, SoundSource.NEUTRAL, 2, (float)
                    mob.getRandom().triangle(1, 0.1));

        mob.setDeltaMovement(0, 0, 0);
        double speed = (targetY - initialY) / (double) t.times.ascend();

        var y = getY();
        double error = targetY - getY();
        double newY = getY() + speed;
        if (Math.abs(error) <= speed)
            newY = targetY;

        mob.setPosRaw(getX(), newY, getZ());

        if (getY() == targetY)
        {
            mob.discard();
        }
    };
}
