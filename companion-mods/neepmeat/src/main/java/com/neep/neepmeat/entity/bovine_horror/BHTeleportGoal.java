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

package com.neep.neepmeat.entity.bovine_horror;

import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BHTeleportGoal extends Goal
{
    private final BovineHorrorEntity mob;
    private int attempts = 0;
    private boolean teleported;
    private final int maxAttempts;
    private final double targetRange;

    private long lastTeleport;
    private final int cooldown;

    public BHTeleportGoal(@NotNull BovineHorrorEntity mob, int maxAttempts, double targetRange, int cooldown)
    {
        this.mob = mob;
        this.maxAttempts = maxAttempts;
        this.targetRange = targetRange;
        this.cooldown = cooldown;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse()
    {
        if (mob.level().getGameTime() - lastTeleport < cooldown)
        {
            return false;
        }

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive())
        {
            return false;
        }

        double dist = target.distanceTo(mob);
        if (dist > targetRange || !mob.hasLineOfSight(target))
            return true;

        return false;
    }

    @Override
    public void start()
    {
        super.start();
        attempts = 0;
        teleported = false;
    }

    @Override
    public void tick()
    {
        ++attempts;

        LivingEntity target = mob.getTarget();
        if (target != null)
        {
            Vec3 targetPos = target.position();
            double currentDistance = mob.distanceTo(target);

            double r = Math.random() * targetRange;
            double t = Math.random() * Math.PI * 2;

            // Get the mob's box centred at 0 0 0
            AABB originBox = mob.getBoundingBox().move(mob.position());
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(targetPos.x + r * Math.sin(t), targetPos.y, targetPos.z + r * Math.cos(t));
            int count = 0;
            while (!mob.level().noCollision(originBox.move(mutable)))
            {
                mutable.setWithOffset(mutable, Direction.UP);
                count++;

                if (count >= targetRange)
                {
                    return;
                }
            }

            Vec3 newPos =  new Vec3(mutable.getX(), mutable.getY(), mutable.getZ());

            double newDistance = targetPos.distanceTo(newPos);

            if (newDistance < currentDistance)
            {
                mob.teleportTo(newPos.x, newPos.y, newPos.z);
                lastTeleport = mob.level().getGameTime();
                teleported = true;
            }
        }
    }

    @Override
    public boolean canContinueToUse()
    {
        return canUse() && attempts < maxAttempts && !teleported;
    }

    protected static Vec3 findTeleportPos(Level world, Vec3 v, Entity mob)
    {
        BlockPos.MutableBlockPos mutable = BlockPos.containing(v).mutable();
        while (world.getBlockState(mutable).isSolid())
        {
            mutable.setWithOffset(mutable, Direction.UP);
        }
        return new Vec3(v.x, mutable.getY(), v.z);
    }
}
