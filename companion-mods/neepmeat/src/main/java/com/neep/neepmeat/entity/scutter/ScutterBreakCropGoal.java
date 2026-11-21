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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ScutterBreakCropGoal extends Goal
{
    private long lastStartTime = 0;

    private final FarmingScutter entity;
    private final Level world;

    public ScutterBreakCropGoal(FarmingScutter entity)
    {
        this.entity = entity;
        this.world = entity.level();
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean requiresUpdateEveryTick()
    {
        return false;
    }

    @Override
    public void tick()
    {
    }

    @Override
    public boolean canUse()
    {
        if (world.getGameTime() < lastStartTime + 10)
            return false;

        return canHarvest(world, entity.blockPosition()) ||
                canHarvest(world, entity.blockPosition().above());
    }

    @Override
    public void start()
    {
        lastStartTime = world.getGameTime();
        if (!tryHarvest(world, entity.blockPosition()))
        {
            tryHarvest(world, entity.blockPosition().above());
        }
    }

    @Override
    public boolean canContinueToUse()
    {
        return false;
    }

    private boolean tryHarvest(Level world, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        if (canHarvest(world, pos))
        {
            entity.harvest(world, pos, state, state.getBlock());
            return true;
        }
        return false;
    }

    private boolean canHarvest(Level world, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        return FarmingScutter.isGrownCrop(state);
    }

}
