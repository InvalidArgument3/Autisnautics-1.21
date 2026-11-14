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

package com.neep.neepmeat.entity.scutter;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

public class ScutterFindFarmlandGoal extends Goal
{
    private final FarmingScutter entity;
    private final int range;
    private final int maxYDiff;
    private BlockPos targetPos;

    public ScutterFindFarmlandGoal(FarmingScutter entity, int range, int maxYDiff)
    {
        this.entity = entity;

        this.range = range;
        this.maxYDiff = maxYDiff;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse()
    {
        return false;
    }

    protected boolean findTargetPos()
    {
        BlockPos blockPos = entity.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int k = 0; k <= this.maxYDiff; k = k > 0 ? -k : 1 - k)
        {
            for (int l = 0; l < this.range; ++l)
            {
                for (int m = 0; m <= l; m = m > 0 ? -m : 1 - m)
                {
                    for (int n = m < l && m > -l ? l : 0; n <= l; n = n > 0 ? -n : 1 - n)
                    {
                        mutable.setWithOffset(blockPos, m, k - 1, n);
//                        if (entity.isInWalkTargetRange(mutable) && this.isTargetPos(this.entity.getWorld(), mutable))
                        {
                            this.targetPos = mutable.immutable();
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
