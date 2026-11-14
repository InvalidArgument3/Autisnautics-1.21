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

package com.neep.neepmeat.entity.follower;

import net.minecraft.core.HolderLookup;

import com.neep.neepmeat.entity.LimbEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FollowerEntity extends PathfinderMob
{
    private final List<LimbEntity> limbs = new ArrayList<>();

    @Nullable
    private UUID targetUUID;
    private int maxLimbs = 9;
    private int limbCooldown = 10;
    private int iterations = 3;

    public FollowerEntity(EntityType<? extends FollowerEntity> type, Level world)
    {
        super(type, world);
        if (world.isClientSide())
        {
            discard();
        }
    }

    public static AttributeSupplier.Builder createFollowerAttributes()
    {
        return createMobAttributes().add(Attributes.FOLLOW_RANGE, 64.0);
    }

    protected SoundEvent getSwimSound()
    {
        return SoundEvents.HOSTILE_SWIM;
    }

    protected SoundEvent getSwimSplashSound()
    {
        return SoundEvents.HOSTILE_SPLASH;
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return SoundEvents.HOSTILE_HURT;
    }

    protected SoundEvent getDeathSound()
    {
        return SoundEvents.HOSTILE_DEATH;
    }

    @Override
    protected void registerGoals()
    {
        targetSelector.addGoal(1, new FollowerFindTargetGoal<>(this, p -> true));
        targetSelector.addGoal(0, new FollowerActiveTargetGoal(this, Player.class));
        goalSelector.addGoal(0, new FollowerFollowGoal(this, 0.5, 1));
    }

    @Override
    public int getMaxFallDistance()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean hurt(DamageSource source, float amount)
    {
        if (source.is(DamageTypeTags.IS_FALL))
            return false;

        if (!source.isCreativePlayer() && amount != Float.MAX_VALUE)
        {
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(net.minecraft.server.level.ServerEntity entity)
    {
        return super.getAddEntityPacket(entity);
    }

    @Override
    public void tick()
    {
        super.tick();

        var it = limbs.iterator();
        while (it.hasNext())
        {
            LimbEntity limb = it.next();
            if (limb.isRemoved())
                it.remove();
        }

        int limbsToAdd = maxLimbs - limbs.size();

        limbCooldown = Math.max(0, limbCooldown - 1);

        if (limbsToAdd > 0 && limbCooldown == 0 && !level().isClientSide())
        {
            LimbEntity limb = new LimbEntity(level(), 50 + random.nextInt(20), false);

            double radius = 2;
            double x = getX() + (random.nextFloat() - 1) * radius;
            double z = getZ() + (random.nextFloat() - 1) * radius;

//            BlockPos collisionPos = BlockPos.ofFloored(x, getBlockY(), z);

            limb.setYRot(random.nextInt(360));
            limb.setPosRaw(x, getY() + 0.1, z);
            limbs.add(limb);
            level().addFreshEntity(limb);
            limbCooldown = 5 + random.nextInt(20);
        }

        if (getTarget() != null)
        {
            double targetDist = distanceTo(getTarget());
            if (targetDist < 1.4)
            {
                teleportRandomly(position());
                iterations = Math.max(0, iterations - 1);

                if (iterations == 0)
                    discard();
            }
            else if (targetDist > 30)
            {
                teleportRandomly(getTarget().position());
            }
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state)
    {
    }

    public void teleportRandomly(Vec3 around)
    {
        if (!this.level().isClientSide() && this.isAlive())
        {
            double d = around.x() + (this.random.nextDouble() - 0.5) * 64.0;
            double e = around.y() + (double) (this.random.nextInt(64) - 32);
            double f = around.z() + (this.random.nextDouble() - 0.5) * 64.0;
            teleportTo(d, e, f);
        }
    }

    private boolean teleportTo(double x, double y, double z)
    {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, y, z);

        while (mutable.getY() > this.level().getMinBuildHeight() && !this.level().getBlockState(mutable).blocksMotion())
        {
            mutable.move(Direction.DOWN);
        }

        BlockState blockState = this.level().getBlockState(mutable);
        boolean bl = blockState.blocksMotion();
        boolean bl2 = blockState.getFluidState().is(FluidTags.WATER);
        if (bl && !bl2)
        {
            return this.randomTeleport(x, y, z, false);
        }
        else
        {
            return false;
        }
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag nbt)
    {
        super.saveWithoutId(nbt);
        if (targetUUID != null)
            nbt.putUUID("persistent_target", targetUUID);

        nbt.putInt("iterations", iterations);

        return nbt;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("persistent_target"))
            this.targetUUID = nbt.getUUID("persistent_target");
        else
            this.targetUUID = null;

        this.iterations = nbt.getInt("iterations");
    }

    public void setPersistentTarget(Player player)
    {
        targetUUID = player.getUUID();
    }

    @Override
    public boolean canCollideWith(Entity other)
    {
        return false;
    }

    @Override
    protected void doPush(Entity entity)
    {
    }

    @Override
    public void push(Entity entity)
    {
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence()
    {
        return true;
    }

    public void setIterations(int its)
    {
        this.iterations = its;
    }

    private static class FollowerFindTargetGoal<T extends Player> extends Goal
    {
        private final FollowerEntity mob;
        private final TargetingConditions predicate;
        protected Player targetEntity;

        public FollowerFindTargetGoal(FollowerEntity mob, Predicate<LivingEntity> predicate)
        {
            this.mob = mob;
            this.predicate = TargetingConditions.forCombat().selector(predicate);
        }

        protected void findClosestTarget()
        {
            targetEntity = mob.level().getNearestPlayer(predicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        }

        @Override
        public boolean canUse()
        {
            if (mob.targetUUID == null && mob.getRandom().nextInt(reducedTickDelay(10)) != 0)
            {
                return false;
            }
            else
            {
                this.findClosestTarget();
                return targetEntity != null;
            }
        }

        @Override
        public void start()
        {
            mob.setPersistentTarget(targetEntity);
            super.start();
        }
    }

    // Looks for the targeted player in the world based on the stored UUID
    private static class FollowerActiveTargetGoal extends Goal
    {
        private final FollowerEntity mob;

        public FollowerActiveTargetGoal(FollowerEntity mob, Class<Player> targetClass)
        {
            this.mob = mob;
        }

        @Override
        public boolean canUse()
        {
            return (mob.getTarget() == null || !mob.getTarget().isAlive()) && mob.targetUUID != null;
        }

        @Override
        public void start()
        {
            Player playerEntity = mob.level().getPlayerByUUID(mob.targetUUID);
            if (playerEntity != null)
            {
                mob.setTarget(playerEntity);
            }
        }

        @Override
        public boolean canContinueToUse()
        {
            return false;
        }
    }
}
