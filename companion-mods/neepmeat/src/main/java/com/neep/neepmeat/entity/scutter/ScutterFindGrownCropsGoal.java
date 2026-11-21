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

import com.neep.neepmeat.util.IterateRandomly;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

// For use when the scutter is already on farmland
public class ScutterFindGrownCropsGoal extends Goal
{
    private final FarmingScutter entity;
    private final Level world;
    private final int interval;

    private long lastTime = 0;

    public ScutterFindGrownCropsGoal(FarmingScutter entity, int interval)
    {
        this.entity = entity;
        this.world = entity.level();
        this.interval = interval;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse()
    {
        return world.getGameTime() - interval > lastTime
                && !entity.needsEmptying()
                && entity.getTargets().isEmpty();
    }

    @Override
    public void start()
    {
        entity.addTargets(findNearest(64));
        lastTime = world.getGameTime();
    }

    @Override
    public boolean canContinueToUse()
    {
        return false;
    }

    @Override
    public boolean isInterruptable()
    {
        return true;
    }

    private List<BlockPos> findNearest(int maxCoverage)
    {
        BlockPos origin = entity.blockPosition();

        Queue<BlockPos> posQueue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        List<BlockPos> result = new ArrayList<>();

        posQueue.add(origin);
        posQueue.add(origin.below());

        int blocks = 0;
        while (blocks < maxCoverage && !posQueue.isEmpty())
        {
            BlockPos current = posQueue.poll();

            BlockPos.MutableBlockPos mutable = current.mutable();
            for (int dir : new IterateRandomly(6))
            {
                Direction direction = Direction.values()[dir];
                if (direction.getAxis().isVertical())
                    continue;

                mutable.setWithOffset(current, direction);

                if (!visited.contains(mutable))
                {
                    visited.add(mutable.immutable());

                    BlockState offsetState = world.getBlockState(mutable);
                    if (FarmingScutter.hasMoisture(offsetState))
                    {
//                        if (world instanceof ServerWorld serverWorld)
//                        {
//                            serverWorld.spawnParticles(ParticleTypes.COMPOSTER, mutable.getX() + 0.5, mutable.getY() + 1.5, mutable.getZ() + 0.5, 1, 0.3, 0.3, 0.3, 0.1);
//                        }
                        posQueue.add(mutable.immutable());
                        BlockPos up = mutable.above();
                        if (FarmingScutter.isGrownCrop(world.getBlockState(up)))
                        {
                            result.add(up);
                        }
                    }
                }
            }

            blocks++;
        }

        return result;
    }
}
