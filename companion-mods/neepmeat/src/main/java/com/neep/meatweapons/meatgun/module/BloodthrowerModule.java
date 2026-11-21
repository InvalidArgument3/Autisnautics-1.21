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

import com.neep.meatweapons.entity.BulletDamageSource;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.meatweapons.particle.MWGraphicsEffects;
import com.neep.meatweapons.particle.MWParticles;
import com.neep.meatweapons.particle.MuzzleFlashParticleType;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.util.NMMaths;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.neep.meatweapons.meatgun.module.BosherModule.getRayTargets;

public class BloodthrowerModule extends ShooterModule
{
    private final RandomSource shotRandom = RandomSource.create();

    public BloodthrowerModule(RootModuleHolder.Listener listener)
    {
        super(listener, AmmunitionType.BLOOD);
    }

    public BloodthrowerModule(RootModuleHolder.Listener listener, CompoundTag nbt)
    {
        this(listener);
        readNbt(nbt, null);
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.BLOODTHROWER;
    }

    @Override
    public void tick(Player player)
    {
        cooldown = Math.max(0, cooldown - 1);
    }

    @Override
    public void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        if (cooldown == 0 && allowedToUse(player))
        {
            if (consume(player.getInventory(), player, hand))
            {
                cooldown = maxCooldown;
                fireBeam(world, player, stack, pitch, yaw, hand);
            }
        }
    }

    @Override
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        trigger(world, player, stack, id, pitch, yaw, handType, hand);
    }

    protected void fireBeam(Level world, Player player, ItemStack stack, double pitchd, double yawd, InteractionHand hand)
    {
        Vec3 pos = player.getEyePosition();
        Vec3 transform = getMuzzleOffset(player, stack, hand).xRot((float) -pitchd).yRot((float) -yawd);
        pos = pos.add(transform);

        List<Entity> targets = hitScan(player, pos, pitchd, yawd, 5, 40, 4, shotRandom);
        for (var target : targets)
        {
            if (target.isAlive())
            {
                target.hurt(BulletDamageSource.create(world, player, 0.0f, false), 0.5f);
                target.invulnerableTime = 0;
            }
        }

        world.playSound(null, player, NMSounds.BLOODTHROWER_ACTIVE.get(), SoundSource.PLAYERS, 1f, 1f);
        if (world instanceof ServerLevel serverWorld)
        {
            Vector4d v = new Vector4d(0, 0, -13 / 16f, 1);
            v.mul(this.transform.globalTransform);
            serverWorld.sendParticles(
                    new MuzzleFlashParticleType.MuzzleFlashParticleEffect(MWParticles.BLOOD_MUZZLE_FLASH, player, v.x, v.y, v.z, 2.2f, 1, hand)
                    , pos.x(), pos.y(), pos.z(),
                    1, 0, 0, 0, 0.1);
        }
    }

    private static List<Entity> hitScan(@NotNull LivingEntity caster, Vec3 start, double pitch, double yaw, int numRays,
                                       double perturb, double distance, RandomSource random)
    {
        perturb = Math.toRadians(perturb);

        // Modify the main ray to account for block collision
        Vec3 end = start.add(NMMaths.getRotationVector(pitch, yaw).scale(distance));
        ClipContext ctx = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster);
        BlockHitResult blockResult = caster.level().clip(ctx);
        if (blockResult.getType() == HitResult.Type.BLOCK)
        {
            end = blockResult.getLocation();
            distance = end.distanceTo(start);
        }

        // Form list of vectors from perturbed pitch and yaw
        List<Vec3> rays = new ArrayList<>(numRays);
        for (int i = 0; i < numRays; ++i)
        {
            double yaw1 = yaw + perturb * (random.nextFloat() - 0.5);
            double pitch1 = pitch + perturb * (random.nextFloat() - 0.5);

            Vec3 ray = NMMaths.getRotationVector(pitch1, yaw1).scale(distance);
            rays.add(ray);

            spawnParticle((ServerLevel) caster.level(), start, ray.normalize());
        }


        Predicate<Entity> entityFilter = entity -> !entity.isSpectator() && entity.isPickable();

        List<Entity> targets = new ArrayList<>();
        for (var hit : getRayTargets(caster, start, end, rays, distance, entityFilter, 0.1))
        {
            if (hit.getType() == HitResult.Type.ENTITY)
            {
                targets.add(((EntityHitResult) hit).getEntity());
            }
//            syncBeamEffect((ServerWorld) caster.getWorld(), start, hit.getPos(), 0.2f, 9, 50);
        }

        return targets;
    }

    private static void spawnParticle(ServerLevel world, Vec3 start, Vec3 ray)
    {
        world.sendParticles(MWParticles.BLOODTHROWER_SPLASH, start.x, start.y, start.z, 0, ray.x, ray.y, ray.z, 0.8);
    }

    private static void syncBeamEffect(ServerLevel world, Vec3 pos, Vec3 end, float width, int maxTime, double showRadius)
    {
        Vec3 col = new Vec3(214, 175, 32);
        for (ServerPlayer player : PlayerLookup.around(world, pos, showRadius))
        {
            MWGraphicsEffects.syncBeamEffect(player, MWGraphicsEffects.BULLET_TRAIL, world, pos, end, col, 0.1f, 1);
        }
    }

    @Override
    protected void fireWithModifiers(Level world, Player shooter, ItemStack stack, double pitch, double yaw, InteractionHand hand)
    {
        var origin = getFireOrigin(shooter, stack, pitch, yaw);
        fireBeam(world, shooter, origin, hand);
    }
}
