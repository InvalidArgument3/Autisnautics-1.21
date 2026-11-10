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

package com.neep.neepmeat.entity;

import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.util.NMMaths;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MachineWormEntity extends Monster implements WormSegment
{
    private final List<Segment> segments = new ObjectArrayList<>();
    private final List<Vec3> prevPositions = new ObjectArrayList<>();

    public MachineWormEntity(EntityType<? extends Monster> entityType, Level world)
    {
        super(entityType, world);

        int segmentCount = 3;
        for (int i = 0; i < segmentCount; ++i)
        {
            float scale = Mth.lerp((float) i / segmentCount, 1f, 0.4f);

            var segment = new Segment(this, !segments.isEmpty() ? segments.get(i - 1) : this, world, scale);
//            world.spawnEntity(segment);
            segments.add(i, segment);
        }
    }

    @Override
    protected void registerGoals()
    {
//        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(8, new MeleeAttackGoal(this, 0.3, false));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.3));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet)
    {
        super.recreateFromPacket(packet);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return super.getAddEntityPacket();
    }

    @Override
    public void tick()
    {
        super.tick();

        if (level().getGameTime() % 4 == 0)
        {
            prevPositions.add(position());
        }

        float bodyYaw = getYRot();
        Vec3 facing = calculateViewVector(0, bodyYaw);

        for (int i = 0; i < segments.size(); ++i)
        {
            var segment = segments.get(i);

            segment.xOld = segment.getX();
            segment.yOld = segment.getY();
            segment.zOld = segment.getZ();
            segment.yRotO = segment.getViewYRot(0);

            segment.updateLink();

            segment.tick();

        }
    }

    private static void updateLink(WormSegment parent, Segment child)
    {
        // So many objects being created... Argh.

        float parentYaw = parent.getSegmentYaw(1);
        Vec3 parentBackFacing = NMMaths.getRotationVector(0, Mth.DEG_TO_RAD * parentYaw).scale(-1);
        float parentRadius = parent.getSegmentDiameter() / 2;
        float childRadius = child.scale / 2;
        Vec3 connectionPoint = parent.getSegmentPos().add(parentBackFacing.scale(parentRadius));

        Vec3 connectionToChild = child.position().subtract(connectionPoint);
//        double distanceTo = connectionToChild.length();

        float angleLimit = 30;

        // This is the correct magnitude, but the calculation does not indicate whether the second angle is clockwise or anticlockwise
//        float angleBetween1 = MathHelper.DEGREES_PER_RADIAN * (float) angle(parentBackFacing, connectionToChild);

        float c2cYaw = -NMMaths.wrapDegreesPositive((float) (Mth.RAD_TO_DEG * Math.atan2(connectionToChild.x, connectionToChild.z)));
        float parentBackYaw = NMMaths.wrapDegreesPositive(parentYaw + 180);

        float newAngle = clampAngle(c2cYaw, parentBackYaw - angleLimit, parentBackYaw + angleLimit);
        Vec3 childOffset = NMMaths.getRotationVector(0, Mth.DEG_TO_RAD * newAngle);
        childOffset = new Vec3(childOffset.x, connectionToChild.y, childOffset.z).normalize().scale(childRadius);

        float childYaw = 180 - (float) (Mth.RAD_TO_DEG * (Math.atan2(childOffset.x, childOffset.z)));
        Vec3 childPos = connectionPoint.add(childOffset);
        child.absMoveTo(childPos.x, childPos.y, childPos.z, childYaw, 0);
    }

    private static float differenceBetweenAngles(float deg1, float deg2)
    {
        float normDeg = (deg1 - deg2) % 360;
        float abs = Math.min(360 - normDeg, normDeg);

        return abs;
    }

    private static float clampAngle(float deg, float min, float max)
    {
        float nMin = Mth.wrapDegrees(min - deg);
        float nMax = Mth.wrapDegrees(max - deg);

        if (nMin <= 0 && nMax >= 0)
        {
            return deg;
        }
        if (Math.abs(nMin) < Math.abs(nMax))
            return min;

        return max;
    }


    private static double angle(Vec3 v1, Vec3 v2)
    {
        return Math.acos(v1.dot(v2) / (v1.length() * v2.length()));
    }

    public static AttributeSupplier createAttributes()
    {
        return Monster.createMonsterAttributes().build();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return NMSounds.MECHANICAL_MACHINE_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return NMSounds.MULTIBLOCK_DISASSEMBLE;
    }

    @Override
    public boolean requiresCustomPersistence()
    {
        return true;
    }

    public List<Segment> getSegments()
    {
        return segments;
    }

    @Override
    public float getSegmentYaw(float tickDelta)
    {
        return getViewYRot(tickDelta);
    }

    @Override
    public float getSegmentDiameter()
    {
        return 1;
    }

    @Override
    public Vec3 getSegmentPos()
    {
        return position();
    }

    public static class Segment extends Entity implements WormSegment
    {
        private final MachineWormEntity head;
        private final WormSegment parent;
        public final float scale;

        public Segment(MachineWormEntity head, WormSegment parent, Level world, float scale)
        {
            super(head.getType(), world);
            this.head = head;
            this.parent = parent;
            this.scale = scale;
        }

        @Override
        public void tick()
        {
            super.tick();
            if (head.isRemoved())
                discard();

        }

        public void updateLink()
        {
            MachineWormEntity.updateLink(parent, this);
        }

        @Override
        protected void defineSynchedData()
        {

        }

        @Override
        protected void readAdditionalSaveData(CompoundTag nbt)
        {

        }

        @Override
        protected void addAdditionalSaveData(CompoundTag nbt)
        {

        }

        @Override
        public boolean hurt(DamageSource source, float amount)
        {
            return head.hurt(source, amount);
        }

        @Override
        public boolean shouldBeSaved()
        {
            return false;
        }

        @Override
        public float getSegmentYaw(float tickDelta)
        {
            return getViewYRot(tickDelta);
        }

        @Override
        public float getSegmentDiameter()
        {
            return scale;
        }

        @Override
        public Vec3 getSegmentPos()
        {
            return position();
        }

        @Override
        public boolean shouldRenderAtSqrDistance(double distance)
        {
            return false;
        }
    }
}
