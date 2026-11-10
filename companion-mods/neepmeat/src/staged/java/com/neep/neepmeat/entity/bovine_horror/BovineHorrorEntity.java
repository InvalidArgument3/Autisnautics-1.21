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

import net.minecraft.core.HolderLookup;

import com.neep.neepmeat.entity.AnimationSyncable;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.util.SightUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BovineHorrorEntity extends Monster implements AnimationSyncable, GeoEntity
{
    private final AnimatableInstanceCache instanceCache = GeckoLibUtil.createInstanceCache(this);

    protected static final EntityDataAccessor<Integer> SYNC_ID = SynchedEntityData.defineId(BovineHorrorEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> VISIBILITY = SynchedEntityData.defineId(BovineHorrorEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Boolean> PHASE2 = SynchedEntityData.defineId(BovineHorrorEntity.class, EntityDataSerializers.BOOLEAN);

//    protected static final AnimationBuilder IDLE_ANIM = new AnimationBuilder().addAnimation("animation.horror.wave", ILoopType.EDefaultLoopTypes.LOOP);

    public float prevVisibility = 0;

    protected boolean updateGoals = true;
    protected boolean phase2 = false;

    protected final BHMeleeAttackGoal meleeAttackGoal = new BHMeleeAttackGoal(this);

    private final ServerBossEvent bossBar = new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    protected final AnimationSyncable.AnimationQueue animationQueue = new AnimationSyncable.AnimationQueue();

    public BovineHorrorEntity(EntityType<? extends Monster> entityType, Level world)
    {
        super(entityType, world);
    }

    public static AttributeSupplier.Builder createLivingAttributes()
    {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200)
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25f)
                .add(Attributes.ARMOR_TOUGHNESS, 2.5)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        super.defineSynchedData();
        entityData.define(SYNC_ID, 0);
        entityData.define(VISIBILITY, 0.0f);
        entityData.define(PHASE2, false);
    }

    public int getSyncId()
    {
        return entityData.get(SYNC_ID);
    }


    protected void updateGoals()
    {
        goalSelector = new GoalSelector(level().getProfilerSupplier());
        targetSelector = new GoalSelector(level().getProfilerSupplier());

        if (isPhase2())
        {
            initPhase2Goals();
        }
        else
        {
            initPhase1Goals();
        }

        updateGoals = false;
    }

    protected void initPhase1Goals()
    {
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));

        goalSelector.addGoal(2, meleeAttackGoal);
        goalSelector.addGoal(2, new BHMoveToTargetGoal(this, 0.6, true));
        goalSelector.addGoal(2, new BHAcidAttackGoal(this, 3, 2));
    }

    protected void initPhase2Goals()
    {
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));

        goalSelector.addGoal(3, new BHBeamGoal(this));
        goalSelector.addGoal(2, new BHTeleportGoal(this, 10, 20, 60));

        bossBar.setDarkenScreen(true);
    }

    @Override
    public boolean doHurtTarget(Entity target)
    {
        syncNearby("animation.horror.melee");
        return super.doHurtTarget(target);
    }


    @Override
    public void knockback(double strength, double x, double z)
    {
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player)
    {
        super.startSeenByPlayer(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player)
    {
        super.stopSeenByPlayer(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet)
    {
        super.recreateFromPacket(packet);
        Vec3 origin = position().add(0, 1.5, 0);
        for (int i = 0; i < 150; ++i)
        {
            spawnParticle(level(), NMParticles.BODY_COMPOUND_SHOWER, random, origin);
        }
    }

    public void spawnParticle(Level world, ParticleOptions effect, RandomSource random, Vec3 origin)
    {
        double r = random.nextFloat() * 1.5 + 0.5;
        double pitch = (random.nextFloat() + 0.5) * 2 * Math.PI / 2;
        double yaw = (random.nextFloat() + 0.5) * 2 * Math.PI;
        double px = origin.x + r * Math.sin(yaw);
        double pz = origin.z + r * Math.cos(yaw);
        double py = origin.y + Math.sin(pitch);

        double vx = px - origin.x * 1;
        double vy = py - origin.y * 1;
        double vz = pz - origin.z * 1;

        level().addParticle(effect, px, py, pz, vx, vy, vz);
    }

    @Override
    public boolean isInvisibleTo(Player player)
    {
        float visibility = level().isClientSide() ? prevVisibility : getVisibility();
        return super.isInvisibleTo(player) || (!SightUtil.canPlayerSee(player) && visibility == 0);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData entityData, @Nullable CompoundTag entityNbt)
    {
        updateGoals();
        return super.finalizeSpawn(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (updateGoals)
            updateGoals();

        if (getHealth() < getMaxHealth() / 2 && !entityData.get(PHASE2) && !phase2)
        {
            entityData.set(PHASE2, true);
            phase2 = true;

            goalSelector.removeAllGoals(g -> true);
            goalSelector.addGoal(1, new BHPhaseActionGoal(this));
            navigation.stop();
            bossBar.setDarkenScreen(true);
        }

        if (!level().isClientSide())
        {
            prevVisibility = getVisibility();
            if (level().getGameTime() % 60 == 0)
            {
                float p = random.nextFloat();
                if (p > 0.5 && getVisibility() == 0)
                {
                    setVisibility(0.9f);
                }
            }

            if (isAlive())
            {
                setVisibility(Math.max(0, getVisibility() - 0.1f));
            }
        }
    }

    @Override
    public void die(DamageSource damageSource)
    {
        super.die(damageSource);
        setVisibility(1);

        if (level().isClientSide())
        {
            for (int i = 0; i < 100; ++i)
            {
                level().addParticle(NMParticles.BODY_COMPOUND_SHOWER,
                        getX() + (random.nextFloat() - 0.5) * 3,
                        getY() + (random.nextFloat()) * 2 + 0.5,
                        getZ() + (random.nextFloat() - 0.5) * 3,
                        0, 0.1, 0
                );
            }
        }
    }

    @Override
    protected void customServerAiStep()
    {
        this.bossBar.setProgress(getHealth() / getMaxHealth());
        super.customServerAiStep();

    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag nbt)
    {
        super.saveWithoutId(nbt);
        boolean phase2 = entityData.get(PHASE2);
        nbt.putBoolean("phase2", phase2);
        return nbt;
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.entityData.set(PHASE2, nbt.getBoolean("phase2"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return NMSounds.BH_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return NMSounds.HOUND_DEATH.get();
    }

    public float getVisibility()
    {
        return entityData.get(VISIBILITY);
    }

    public void setVisibility(float visibility)
    {
        entityData.set(VISIBILITY, visibility);
    }

    public float getAttackRange(Entity entity)
    {
        return 4;
    }

    public boolean canMelee(Entity entity)
    {
        return distanceTo(entity) <= getAttackRange(entity);
    }

    public boolean isInRange(Entity entity)
    {
        return distanceTo(entity) <= getAttackRange(entity) * 0.5;
    }

    public boolean isPhase2()
    {
        return entityData.get(PHASE2);
    }

    @Override
    public AnimationQueue getQueue()
    {
        return animationQueue;
    }

    @Override
    public boolean requiresCustomPersistence()
    {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source)
    {
        return super.isInvulnerableTo(source)
                || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypes.IN_WALL);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(
                new AnimationController<>(this, "move", 5, this::moveController),
                new AnimationController<>(this, "attack", 5, this::attackControl));
    }

    private <T extends GeoEntity> PlayState attackControl(final AnimationState<T> event)
    {
        if (!animationQueue.isEmpty() && event.getController().getAnimationState() == AnimationController.State.STOPPED)
        {
            event.setAndContinue(RawAnimation.begin().thenPlay(animationQueue.poll()));
        }

        return PlayState.CONTINUE;
    }

    private <E extends BovineHorrorEntity> PlayState moveController(final AnimationState<E> event)
    {
        if (event.isMoving())
        {
            event.setAndContinue(RawAnimation.begin().thenLoop("animation.horror.wave"));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache()
    {
        return instanceCache;
    }
}