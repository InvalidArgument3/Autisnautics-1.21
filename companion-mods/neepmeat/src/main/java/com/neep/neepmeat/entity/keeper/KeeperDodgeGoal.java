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

package com.neep.neepmeat.entity.keeper;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class KeeperDodgeGoal extends Goal
{
    protected long lastDodge;
    protected float range;
    protected int ticks;
    protected final KeeperEntity entity;
    protected final int maxTicks = 20;

    public KeeperDodgeGoal(KeeperEntity entity, float range)
    {
//        super(mob, speed, false);
        this.entity = entity;
        this.range = range;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse()
    {
        return
                entity.getLastHurtByMob() != null
                && entity.isPlayerStaring(entity.getLastHurtByMob())
                && entity.level().getGameTime() > lastDodge + maxTicks;
    }

    @Override
    public void start()
    {
        super.start();
        lastDodge = entity.level().getGameTime();
        ticks = 0;
    }

    @Override
    public void stop()
    {
        super.stop();
//        entity.getMoveControl().strafeTo(-2f, entity.getRandom().nextBoolean() ? 4f : -4f);
    }

    @Override
    public void tick()
    {
        super.tick();

        LivingEntity target = entity.getTarget();
        if (ticks == 0 && target != null)
        {
            boolean b1 = entity.getRandom().nextBoolean();

            float angle = (float) (b1 ? -Math.PI / 2 : Math.PI / 2);
            Vec3 v = target.position().subtract(entity.position()).yRot(angle);
            Vec3 newPos = findTeleportPos(entity.level(), target.position().add(v));

            if (entity.level() instanceof ServerLevel serverWorld)
            {
                serverWorld.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.NETHER_WART_BLOCK.defaultBlockState()),
                        entity.getX(), entity.getY(0.5), entity.getZ(), 20, 0.1, 1, 0.1,2);
            }
            entity.teleportToWithTicket(newPos.x, newPos.y, newPos.z);
        }
        ++ticks;
    }

    protected static Vec3 findTeleportPos(Level world, Vec3 v)
    {
        BlockPos.MutableBlockPos mutable = BlockPos.containing(v).mutable();
        while (world.getBlockState(mutable).blocksMotion())
        {
            mutable.setWithOffset(mutable, Direction.UP);
        }
        return new Vec3(v.x, mutable.getY(), v.z);
    }

    @Override
    public boolean canContinueToUse()
    {
        return false;
    }
}
