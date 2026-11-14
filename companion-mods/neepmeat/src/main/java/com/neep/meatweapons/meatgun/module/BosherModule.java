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

package com.neep.meatweapons.meatgun.module;

import com.neep.meatweapons.component.AdsCounter;
import com.neep.meatweapons.entity.BulletEntity;
import com.neep.meatweapons.entity.HitScan;
import com.neep.meatweapons.entity.MeatgunProjectileEntity;
import com.neep.meatweapons.item.BeamEffectProvider;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.SlotType;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.meatweapons.network.MeatgunNetwork;
import com.neep.meatweapons.particle.MWGraphicsEffects;
import com.neep.meatweapons.particle.MWParticles;
import com.neep.meatweapons.particle.MuzzleFlashParticleType;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.util.NMMaths;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BosherModule extends ShooterModule
{
    private final ModuleSlot barrel;

    public BosherModule(RootModuleHolder.Listener listener)
    {
        super(listener, AmmunitionType.BALLISTIC);
        this.barrel = new SimpleModuleSlot(listener, new Matrix4f().translate(0, 0, -9 / 16f), SlotType.BARREL, false);
        setSlots(List.of(barrel));
    }

    public BosherModule(RootModuleHolder.Listener listener, CompoundTag ignore)
    {
        this(listener);
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.BOSHER;
    }

    @Override
    public void tick(Player player)
    {
        cooldown = Math.max(0, cooldown - 1);
    }

    @Override
    public void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        if (id == 1)
        {
            if (cooldown == 0 && allowedToUse(player))
            {
                if (consume(player.getInventory(), player, hand))
                {
                    cooldown = maxCooldown;

                    fireWithModifiers(world, player, stack, pitch, yaw, hand);
                }
            }
        }
    }

    @Override
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        trigger(world, player, stack, id, pitch, yaw, handType, hand);
    }

    @Override
    protected void fireWithModifiers(Level world, Player shooter, ItemStack stack, double pitch, double yaw, InteractionHand hand)
    {
        Vec3 pos = shooter.getEyePosition();
        Vec3 transform = getMuzzleOffset(shooter, stack, hand).xRot((float) -pitch).yRot((float) -yaw);
        pos = pos.add(transform);

        float deviation = listener.getHolder().moduleCache().moduleTypes().contains(MeatgunModules.BASIC_STOCK)
                ? 8 : 16;
        var origins = getRays(pos, 5, pitch, yaw, deviation * Mth.DEG_TO_RAD);

        if (barrel.get().getType() != MeatgunModules.SLOW_BARREL)
        {
            List<HitScan.HitScanResult> targets = hitScan1(shooter, pos, origins, 40, this::syncBeamEffect, 0.1f);

            for (var target : targets)
            {
                applyDamageEffects(world, shooter, target);
            }
        }
        else
        {
            for (ShotOrigin origin : origins)
            {
                float speed = 2f;
                var pos1 = origin.pos();
                var velocity = origin.direction().scale(speed);
                var entity = new BulletEntity(world, pos1.x, pos1.y, pos1.z, velocity.x, velocity.y, velocity.z);
                entity.setNoGravity(false);
                world.addFreshEntity(entity);

                entity.setHomingSpeed(speed / 4, true);
                entity.setOwner(shooter);
                entity.setBaseDamage(getType().attributes().attackDamage());
                entity.setBulletPunch(0.1f);
                entity.setCol(0x880000);
                entity.setMaxCollisions(shotRandom.nextBoolean() ? 1 : 0, 1.5f);
                entity.setRicochetSound(null);
                entity.setMaxDistance(40);
                MeatgunProjectileEntity.setupProjectileAttributes(entity, listener.getHolder(), 3);
            }
        }

        boolean ads = AdsCounter.isADS(shooter, stack, hand);
        MeatgunNetwork.sendRecoil(shooter,
                ads ? 4 : 7
                , 0.4f,0.3f, 0.01f, 4, hand);
        world.playSound(null, shooter, NMSounds.BOSHER_FIRE.get(), SoundSource.PLAYERS, 1f, 1f);
        if (world instanceof ServerLevel serverWorld)
        {
            Vector4d v = new Vector4d(0, 0, -13 / 16f, 1);
            v.mul(this.transform.globalTransform);
            serverWorld.sendParticles(
                    new MuzzleFlashParticleType.MuzzleFlashParticleEffect(MWParticles.BOSHER_MUZZLE_FLASH, shooter, v.x, v.y, v.z, 2.2f, 1, hand)
                    , pos.x(), pos.y(), pos.z(),
                    1, 0, 0, 0, 0.1);
        }
    }

    private List<ShotOrigin> getRays(Vec3 start, int numRays, double pitch, double yaw, float deviation)
    {
        List<ShotOrigin> origins = new ObjectArrayList<>(numRays);

        for (int i = 0; i < numRays; ++i)
        {
            Vector3f o = new Vector3f(0, 0, 1)
                    .rotateY((float) shotRandom.triangle(0, deviation))
                    .rotateX((float) shotRandom.triangle(0, deviation))
                    .rotateX((float) pitch)
                    .rotateY((float) -yaw)
                    ;
            origins.add(new ShotOrigin(start, new Vec3(o)));
        }

        return origins;
    }

    public static List<HitScan.HitScanResult> hitScan1(@NotNull LivingEntity shooter, Vec3 start,
                                         List<ShotOrigin> origins, float distance, BeamEffectProvider gunItem, float margin)
    {
        Level world = shooter.level();
        Predicate<Entity> entityFilter = entity -> !entity.isSpectator() && entity.isPickable();

        List<HitScan.HitScanResult> targets = new ObjectArrayList<>();
        Vec3[] rayHits = new Vec3[origins.size()];

        double maxDistance = 0;
        Vec3 longestRay = Vec3.ZERO;
        for (int i = 0; i < origins.size(); ++i)
        {
            ShotOrigin origin = origins.get(i);

            Vec3 end = start.add(origin.direction().scale(distance));
            ClipContext ctx = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter);
            BlockHitResult blockResult = shooter.level().clip(ctx);
            Vec3 rayHitPos = blockResult.getLocation();
            rayHits[i] = rayHitPos;

            // Find the longest ray to establish the entity catchment box
            double hitRayLength = rayHitPos.distanceTo(start);
            if (hitRayLength > maxDistance)
            {
                maxDistance = hitRayLength;
                longestRay = rayHitPos.subtract(start);
            }
        }

        // Query entities once to reduce complexity and avoid duplicates
        AABB box = shooter.getBoundingBox().expandTowards(longestRay).inflate(1.0, 1.0, 1.0);
        List<Entity> boxEntities = world.getEntities(shooter, box, entityFilter);

        for (var blockHitPos : rayHits)
        {
            Vec3 hitPos = raycastEntities(boxEntities, targets, start, blockHitPos, distance, margin);

            gunItem.syncBeamEffect((ServerLevel) shooter.level(), start, hitPos, 0.2f, 9, 50);
        }

        return targets;
    }

    private static Vec3 raycastEntities(List<Entity> potentialEntities, List<HitScan.HitScanResult> targets, Vec3 start, Vec3 end, float distance, float margin)
    {
        double minDistance = Double.MAX_VALUE;
        Vec3 minHitPos = null;
        @Nullable Entity minEntity = null;
        for (var entity : potentialEntities)
        {
            var hitPos = entity.getBoundingBox().inflate(entity.getPickRadius() + margin).clip(start, end).orElse(null);
            if (hitPos != null)
            {
                double dist = hitPos.distanceTo(start);
                if (dist <= minDistance)
                {
                    minDistance = dist;
                    minEntity = entity;
                    minHitPos = hitPos;
                }
            }
        }

        if (minEntity != null)
        {
            targets.add(new HitScan.HitScanResult(minEntity, minHitPos, end.subtract(start).normalize()));
        }

        return minHitPos != null ? minHitPos : end;
    }

    public static List<Entity> hitScan(@NotNull LivingEntity shooter, Vec3 start, double pitch, double yaw, int numRays,
                                           double perturb, double distance, RandomSource random, BeamEffectProvider gunItem)
    {
        // Modify the main ray to account for block collision
        Vec3 end = start.add(NMMaths.getRotationVector(pitch, yaw).scale(distance));
        ClipContext ctx = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter);
        BlockHitResult blockResult = shooter.level().clip(ctx);
        if (blockResult.getType() == HitResult.Type.BLOCK)
        {
            end = blockResult.getLocation();
            distance = end.distanceTo(start);
        }

        // Form list of vectors from perturbed pitch and yaw
        List<Vec3> rays = new ArrayList<>(numRays);
        for (int i = 0; i < numRays; ++i)
        {
            double yaw1 = yaw + perturb * 0.1 * (random.nextFloat() - 0.5);
            double pitch1 = pitch + perturb * 0.1 * (random.nextFloat() - 0.5);

            Vec3 ray = NMMaths.getRotationVector(pitch1, yaw1).scale(distance);
            rays.add(ray);
        }

        // Use the original ray to find the distance before block collision
        Predicate<Entity> entityFilter = entity -> !entity.isSpectator() && entity.isPickable();

        List<Entity> targets = new ArrayList<>();
        for (var hit : getRayTargets(shooter, start, end, rays, distance, entityFilter, 0.1))
        {
            if (hit.getType() == HitResult.Type.ENTITY)
            {
                targets.add(((EntityHitResult) hit).getEntity());
            }

            gunItem.syncBeamEffect((ServerLevel) shooter.level(), start, hit.getLocation(), 0.2f, 9, 50);
        }

        return targets;
    }

    public static List<HitResult> getRayTargets(LivingEntity shooter, Vec3 startPos, Vec3 mainRayEnd, List<Vec3> rays, double distance, Predicate<Entity> predicate, double margin)
    {
        Level world = shooter.level();
        AABB box = shooter.getBoundingBox().expandTowards(mainRayEnd.subtract(startPos)).inflate(1.0, 1.0, 1.0);

        List<Entity> boxEntities = world.getEntities(shooter, box, predicate);
        List<HitResult> list = new ArrayList<>();
        for (Vec3 ray : rays)
        {
            Vec3 endPos = startPos.add(ray);

            double minDistance = distance;
            @Nullable EntityHitResult minEntity = null;
            for (var entity : boxEntities)
            {
                Vec3 hitPos = entity.getBoundingBox().inflate(entity.getPickRadius() + margin).clip(startPos, endPos).orElse(null);
                if (hitPos != null)
                {
                    double dist = hitPos.distanceTo(startPos);
                    if (dist <= minDistance)
                    {
                        minDistance = dist;
                        minEntity = new EntityHitResult(entity, hitPos);
                    }
                }
            }

            if (minEntity != null)
            {
                list.add(minEntity);
            }
            else
            {
                ClipContext ctx = new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter);
                BlockHitResult blockResult = world.clip(ctx);
                list.add(blockResult);
            }
        }

        return list;
    }

    public void syncBeamEffect(ServerLevel world, Vec3 pos, Vec3 end, float width, int maxTime, double showRadius, boolean full)
    {
        Vec3 col = new Vec3(255, 90, 90);
        for (ServerPlayer player : PlayerLookup.around(world, pos, showRadius))
        {
            MWGraphicsEffects.syncBeamEffect(player, MWGraphicsEffects.BULLET_TRAIL, world, pos, end, col, 0.1f, 1);
        }
    }
}
