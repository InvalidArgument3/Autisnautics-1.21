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

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.api.entity.MultiPartEntity;
import com.neep.neepmeat.util.Bezier;
import com.neep.neepmeat.util.Easing;
import com.neep.neepmeat.util.NMMaths;
import net.minecraft.world.entity.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

import static com.neep.neepmeat.network.NMTrackedData.DOUBLE;

public class WormEntity extends AbstractWormPart implements MultiPartEntity<WormEntity.WormSegment>, GeoEntity
{
    protected static final EntityDataAccessor<String> CURRENT_ACTION = SynchedEntityData.defineId(WormEntity.class, EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<Double> HEAD_X = SynchedEntityData.defineId(WormEntity.class, DOUBLE);
    protected static final EntityDataAccessor<Double> HEAD_Y = SynchedEntityData.defineId(WormEntity.class, DOUBLE);
    protected static final EntityDataAccessor<Double> HEAD_Z = SynchedEntityData.defineId(WormEntity.class, DOUBLE);
    protected static final EntityDataAccessor<Float> HEAD_PITCH = SynchedEntityData.defineId(WormEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> HEAD_YAW = SynchedEntityData.defineId(WormEntity.class, EntityDataSerializers.FLOAT);

    private final AnimatableInstanceCache instanceCache = GeckoLibUtil.createInstanceCache(this);
    private WormAction currentAction;
    protected List<WormSegment> segments = new ArrayList<>(17);
    protected List<WormSegment> tail = new ArrayList<>(16);
    public final WormSegment head;

    @Nullable protected LivingEntity target;

    protected final GoalSelector goalSelector;
//    protected final GoalSelector targetGoalSelector;

    public WormEntity(EntityType<? extends WormEntity> type, Level world)
    {
        super(type, world);
        int length = 16;
        for (int i = 0; i < length; ++i)
        {
            WormSegment segment = new WormSegment(this, 26 / 16f, 16);
            tail.add(segment);
        }
        head = new WormSegment(this, 26 / 16f, 16);
        segments.addAll(tail);
        segments.add(head);

        this.goalSelector = new GoalSelector(level().getProfilerSupplier());
//        this.targetGoalSelector = new GoalSelector(getWorld().getProfilerSupplier());

        if (!level().isClientSide())
        {
            initGoals();
        }
    }

    protected void initGoals()
    {
        this.goalSelector.addGoal(1, new WormTargetGoal(this, TargetingConditions.DEFAULT, 14));
        this.goalSelector.addGoal(2, new WormFullSwingGoal(this, 8));
        this.goalSelector.addGoal(2, new WormBiteGoal(this));
    }


    public void setTarget(LivingEntity target)
    {
        this.target = target;
    }

    public LivingEntity getTarget()
    {
        return target;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        super.defineSynchedData(builder);
        builder.define(CURRENT_ACTION, WormAction.EmptyAction.ID.toString());

        builder.define(HEAD_X, getX());
        builder.define(HEAD_Y, getY() + 15);
        builder.define(HEAD_Z, getZ());
        builder.define(HEAD_PITCH, -90f);
        builder.define(HEAD_YAW, 0f);
    }

    public static AttributeSupplier.Builder createLivingAttributes()
    {
        return Mob.createMobAttributes();
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag nbt)
    {
        super.saveWithoutId(nbt);

        nbt.put("currentAction", WormActions.toNbt(currentAction));
        return nbt;
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);

        currentAction = WormActions.fromNbt(nbt.getCompound("currentAction"), this);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public void tick()
    {
        super.tick();
        Bezier.Cubic3 bezier = new Bezier.Cubic3(10);
        setXRot(-90);

        double y = getY();
        double x = getX();
        double z = getZ();

        if (level().isClientSide())
        {
            updateHead();
        }

        Vec3 headLook = head.position().add(Vec3.directionFromRotation(head.getXRot(), head.getYRot()).scale(-8));

        bezier.setPoint(0, x, y, z);
        bezier.setPoint(1, x, y + 5, z);
        bezier.setPoint(2, headLook.x, headLook.y, headLook.z);
        bezier.setPoint(3, head.getX(), head.getY(), head.getZ());

        for (int i = 0; i < tail.size(); ++i)
        {
            WormSegment segment = tail.get(i);
            float delta = ((float) i) / tail.size();

            double l = bezier.length();
            double t = bezier.tForDistance(delta * bezier.length());
            Vec3 X = bezier.value(t);
            Vec3 U = bezier.derivative(t);

//            double x1 = Bezier.bezier3(delta, x, x, headLook.x, head.getX());
//            double y1 = Bezier.bezier3(delta, y, y + 5, headLook.y, head.getY());
//            double z1 = Bezier.bezier3(delta, z, z, headLook.z, head.getZ());
//
//            double u = Bezier.derivative3(delta, x, x, headLook.x, head.getX());
//            double v = Bezier.derivative3(delta, y, y + 5, headLook.y, head.getY());
//            double w = Bezier.derivative3(delta, z, z, headLook.z, head.getZ());

            Vec2 pitchYaw = NMMaths.rectToPol(U.x, U.y, U.z);

            segment.absMoveTo(X.x, X.y, X.z, pitchYaw.y, pitchYaw.x);
        }

        updateGoalControls();
    }

    @Override
    protected void serverAiStep()
    {
        int i = this.level().getServer().getTickCount() + this.getId();
        if (i % 2 == 0 || this.tickCount <= 1)
        {
//            this.getWorld().getProfiler().push("targetSelector");
//            this.targetSelector.tick();
//            this.getWorld().getProfiler().pop();
            this.level().getProfiler().push("goalSelector");
            this.goalSelector.tick();
            this.level().getProfiler().pop();
        }
        else
        {
//            this.getWorld().getProfiler().push("targetSelector");
//            this.targetSelector.tickGoals(false);
//            this.getWorld().getProfiler().pop();
            this.level().getProfiler().push("goalSelector");
            this.goalSelector.tickRunningGoals(false);
            this.level().getProfiler().pop();
        }
    }

    protected void updateGoalControls()
    {
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, true);
        this.goalSelector.setControlFlag(Goal.Flag.TARGET, true);
    }

    protected Vec3 getNeutralHeadPos()
    {
        return new Vec3(getX(), getY() + 15, getZ());
    }

    private Vec2 getNeutralHeadPitch()
    {
        return new Vec2(-90, 0);
    }

    protected void updateHead()
    {
//        double x = dataTracker.get(HEAD_X);
//        double y = dataTracker.get(HEAD_Y);
//        double z = dataTracker.get(HEAD_Z);
//        float pitch = dataTracker.get(HEAD_PITCH);
//        float yaw = dataTracker.get(HEAD_YAW);
        head.absMoveTo(entityData.get(HEAD_X), entityData.get(HEAD_Y), entityData.get(HEAD_Z),
                entityData.get(HEAD_YAW), entityData.get(HEAD_PITCH));
    }

    @Override
    public void remove(RemovalReason reason)
    {
        segments.forEach(s -> s.remove(reason));
        super.remove(reason);
    }

    protected WormActions.Entry chooseAction()
    {
        return WormActions.random();
    }

    public void setHeadPos(double x, double y, double z)
    {
        entityData.set(HEAD_X, x);
        entityData.set(HEAD_Y, y);
        entityData.set(HEAD_Z, z);
        head.setPosRaw(x, y, z);
    }

    public void setHeadAngles(float pitch, float yaw)
    {
        if (!Float.isNaN(pitch))
        {
            head.setXRot(pitch);
            entityData.set(HEAD_PITCH, pitch);
        }
        if (!Float.isNaN(yaw))
        {
            head.setYRot(yaw);
            entityData.set(HEAD_YAW, yaw);
        }
    }

    protected PlayState controller(final AnimationState<WormEntity> event)
    {
//        String anim = WormActions.getAnimation(dataTracker.get(CURRENT_ACTION));
//        event.getController().setAnimation(WormActions.getAnimation(dataTracker.get(CURRENT_ACTION)));

        return PlayState.CONTINUE;
    }

    @Override
    public Iterable<WormSegment> getParts()
    {
        return segments;
    }

    public void lerpHeadPos(int tick, int totalTicks, Vec3 toPos)
    {
        float delta = (float) tick / totalTicks;
        double headX = Mth.lerp(delta, head.getX(), toPos.x());
        double headY = Mth.lerp(delta, head.getY(), toPos.y());
        double headZ = Mth.lerp(delta, head.getZ(), toPos.z());

        setHeadPos(headX, headY, headZ);
    }

    public void lerpHeadPos(int tick, int totalTicks, Vec3 toPos, Easing.Curve curve)
    {
        double c = curve.apply((double) tick / totalTicks);
        double headX = head.getX() + (toPos.x() - head.getX()) * c;
        double headY = head.getY() + (toPos.y() - head.getY()) * c;
        double headZ = head.getZ() + (toPos.z() - head.getZ()) * c;

        setHeadPos(headX, headY, headZ);
    }

    public void lerpHeadAngles(int tick, int totalTicks, float toPitch, float toYaw)
    {
        float delta = (float) tick / totalTicks;
        float headPitch = Mth.lerp(delta, head.getXRot(), toPitch);
        float headYaw = Mth.lerp(delta, head.getYRot(), toYaw);

        setHeadAngles(headPitch, headYaw);
    }

    public void returnHeadToNeutral(int tick, int totalTicks)
    {
        lerpHeadPos(tick, totalTicks, getNeutralHeadPos());
        Vec2 pitchYaw = getNeutralHeadPitch();
        lerpHeadAngles(tick, totalTicks, pitchYaw.x, pitchYaw.y);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, "controller", 5, this::controller));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache()
    {
        return instanceCache;
    }


    public static class WormSegment extends Entity
    {
        protected final WormEntity parent;
        protected final EntityDimensions partDimensions;

        public WormSegment(WormEntity parent, float width, float height)
        {
            super(parent.getType(), parent.level());
            this.parent = parent;
            this.partDimensions = EntityDimensions.scalable(width, height);
            refreshDimensions();
        }

//        public WormSegment(EntityType<? extends WormSegment> type, World world)
//        {
//
//        }


        @Override
        protected void readAdditionalSaveData(CompoundTag nbt)
        {
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag nbt)
        {
        }

        @Override
        public void setXRot(float pitch)
        {
            super.setXRot(pitch);
        }

        @Override
        public boolean is(Entity entity)
        {
            return this == entity || parent == entity;
        }

        @Override
        public Packet<ClientGamePacketListener> getAddEntityPacket()
        {
            throw new UnsupportedOperationException();
//            return new EntitySpawnS2CPacket(this);
        }

        @Override
        public void recreateFromPacket(ClientboundAddEntityPacket packet)
        {
            super.recreateFromPacket(packet);
        }

        @Override
        public EntityDimensions getDimensions(Pose pose)
        {
            return this.partDimensions;
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder)
        {

        }

        @Override
        public void absMoveTo(double x, double y, double z, float yaw, float pitch)
        {
            this.absMoveTo(x, y, z);
            this.yRotO = getYRot();
            this.xRotO = getXRot();
            this.setYRot(yaw);
            this.setXRot(pitch);
        }

        @Override
        public void absMoveTo(double x, double y, double z)
        {
            double d = Mth.clamp(x, -3.0E7, 3.0E7);
            double e = Mth.clamp(z, -3.0E7, 3.0E7);
            this.xo = getX();
            this.yo = getY();
            this.zo = getZ();
            this.setPos(d, y, e);
        }

        @Override
        public boolean hurt(DamageSource source, float amount)
        {
            return parent.hurt(source, amount);
        }

        @Override
        public boolean shouldBeSaved()
        {
            return false;
        }
    }
}
