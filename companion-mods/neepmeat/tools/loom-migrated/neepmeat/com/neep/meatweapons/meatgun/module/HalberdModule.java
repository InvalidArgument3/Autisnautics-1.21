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

import com.neep.meatlib.network.PacketBufUtil;
import com.neep.meatweapons.entity.HitOnCollideEntity;
import com.neep.meatweapons.interfaces.HookableEntity;
import com.neep.meatweapons.item.BaseGunItem;
import com.neep.meatweapons.item.BeamEffectProvider;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.meatweapons.network.MeatgunNetwork;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.util.NMMaths;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

public class HalberdModule extends MeleeModule
{
    private boolean triggerHeld;
    private int triggerTicks;

    private int swingDownCooldown;
    private int hookGrabCooldown;

    public HalberdModule(RootModuleHolder.Listener listener)
    {
        super(listener, List.of());
    }

    public HalberdModule(RootModuleHolder.Listener listener, CompoundTag nbt)
    {
        this(listener);
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.HALBERD;
    }

    @Override
    public void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        super.trigger(world, player, stack, id, pitch, yaw, handType, hand);

        if (id == 2 && swingDownCooldown == 0)
        {
            fireBeam(world, player, pitch, yaw, 3, hand);
            world.playSound(null, player, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1, 1);
            MeatgunNetwork.SEND_ANIMATION.emitter(player).apply("blade_swing_down", hand, null); // Sword attack
            swingDownCooldown = 15;
        }

        if (id == 1)
        {
            if (player.getVehicle() != null)
            {
                hookWhenMounted(world, player, player.getVehicle(), pitch, yaw, hand);

            }
            else if (player.isSprinting())
            {
                startUpperThrust(player, hand);
            }
        }
    }

    @Override
    protected boolean meleeDamageEntity(Level world, Player player, Entity targetEntity)
    {
        if (super.meleeDamageEntity(world, player, targetEntity))
        {
            return true;
        }

        return false;
    }

    private void startUpperThrust(Player player, InteractionHand hand)
    {
        triggerHeld = true;
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 1, 1);
        MeatgunNetwork.SEND_ANIMATION.emitter(player).apply("upper_thrust", hand, null);
        sync(player, hand);
    }

    @Override
    public void release(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        super.release(world, player, stack, id, pitch, yaw, handType, hand);

        if (id == 1)
        {
            if (triggerTicks >= 10)
            {
                thrustForwards(world, player, pitch, yaw, hand);
            }

            triggerHeld = false;
            triggerTicks = 0;
            sync(player, hand);
//            listener.markDirty(RootModuleHolder.Reason.SAVE_DATA);
        }
    }

    @Override
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        super.tickTrigger(world, player, stack, id, pitch, yaw, handType, hand);

        if (id == 1)
        {
            if (player.isSprinting())
            {
                triggerTicks = Math.min(triggerTicks + 1, 30);

                if (!triggerHeld)
                {
                    startUpperThrust(player, hand);
                }
            }
        }
    }

    @Override
    public void tick(Player player)
    {
        super.tick(player);

        swingDownCooldown = Math.max(0, swingDownCooldown - 1);
        hookGrabCooldown = Math.max(0, hookGrabCooldown - 1);

        if (!player.isSprinting())
        {
            triggerTicks = 0;
            if (triggerHeld)
            {
                triggerHeld = false;
                listener.markDirty(RootModuleHolder.Reason.SAVE_DATA);
            }
        }
    }

    private void sync(Player player, InteractionHand hand)
    {
        MeatgunNetwork.syncModule(player, this, hand);
    }

    protected void hookWhenMounted(Level world, Player player, Entity vehicle, double pitch, double yaw, InteractionHand hand)
    {
        if (hookGrabCooldown > 0)
            return;

        hookGrabCooldown = 5;
        world.playSound(null, player, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.5f, 1);
        Vec3 pos = player.getEyePosition();
        Vec3 end = pos.add(NMMaths.getRotationVector(pitch, yaw).scale(4));
        EntityHitResult target = BaseGunItem.hitScan(player, pos, end, 4, e -> e != vehicle, BeamEffectProvider.NONE, 0.1f).orElse(null);

        if (target != null && target.getEntity() instanceof LivingEntity livingEntity)
        {
            if (player instanceof ServerPlayer serverPlayer)
            {
                hookGrabCooldown = 20;
                world.playSound(null, player, SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH, SoundSource.PLAYERS, 1, 1);

                Vec3 offset = livingEntity.position().subtract(player.position());

                FriendlyByteBuf buf = PacketByteBufs.create();
                PacketBufUtil.writeVec3d(buf, offset);
                MeatgunNetwork.SEND_ANIMATION.emitter(player).apply("hook_grab", hand, buf);

                if (livingEntity.isPassenger() && player.getRandom().nextBoolean())
                {
                    livingEntity.removeVehicle();
                }

//                target.addVelocity(
//                        holder.meatweapons$movementX() * 2,
//                        holder.meatweapons$movementY() + 0.3,
//                        holder.meatweapons$movementZ() * 2
//                );
//
//                target.velocityModified = true;
//                target.velocityDirty = true;

                ((HookableEntity) livingEntity).meatweapons$setHookParent(player, offset);
                ((HookableEntity) livingEntity).meatweapons$setHookTicks(60);
                livingEntity.hurt(world.damageSources().playerAttack(player), 2);
//                BoneStealerModule.apply(player, livingEntity, 2);
            }
        }
        else
        {
            MeatgunNetwork.sendRecoil(player, 7, 0.2f,0.7f, 0.03f, hand);
        }
    }

    protected void thrustForwards(Level world, Player player, double pitch, double yaw, InteractionHand hand)
    {
        double f = 0.7;
//        player.addVelocity(f * Math.sin(pitch), 1.05, f * Math.cos(pitch));
        if (!player.isFallFlying())
        {
            player.push(f * Math.sin(-yaw), 0.4, f * Math.cos(-yaw));
            player.hurtMarked = true;
            player.hasImpulse = true;
        }

        ((HitOnCollideEntity) player).meatweapons$startCollideHit(20, baseDamage + 2f,
                listener.getHolder().containsType(MeatgunModules.BONE_STEALER));

        MeatgunNetwork.sendRecoil(player, 7, 0.2f,0.7f, 0.03f, hand);

    }

    // I can't think of a better place to put this. It needs to be outside the mixin so that it can be hot-swapped.
    public static void onEntityCollide(Player origin, Entity target, int ticksRemaining, float damage, boolean stealBones)
    {
        if (!origin.level().isClientSide() && target.isAlive() && target.isPickable())
        {
            Vec3 velocity = origin.getDeltaMovement();
            if (ticksRemaining > 0 && Math.abs(velocity.x) > 0.05 && Math.abs(velocity.z) > 0.05)
            {
                if (target.hurt(origin.level().damageSources().playerAttack(origin), damage))
                {
                    origin.level().playSound(null, origin.getX(), origin.getY(), origin.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1, 1);
                    if (stealBones)
                        BoneStealerModule.apply(origin, target, damage);
                }
            }
        }
    }

    public static void onOnVehicleMove(ServerboundMoveVehiclePacket packet, Entity entity, ServerLevel serverWorld, double d, double e, double f, double g, double h, double i, float j, float k, double l, double m, double n, double o, double p, boolean bl, boolean bl2)
    {
        NeepMeat.LOGGER.info("l: {}, m: {}, n: {}", l, m, n);
    }


    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        super.writeNbt(nbt);
        nbt.putBoolean("trigger_held", triggerHeld);
        nbt.putInt("trigger_ticks", triggerTicks);
        nbt.putInt("down_cooldown", swingDownCooldown);
        nbt.putInt("grab_cooldown", hookGrabCooldown);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt, @Nullable Matrix4f global)
    {
        super.readNbt(nbt, global);
        this.triggerHeld = nbt.getBoolean("trigger_held");
        this.triggerTicks = nbt.getInt("trigger_ticks");
        this.swingDownCooldown = nbt.getInt("down_cooldown");
        this.hookGrabCooldown = nbt.getInt("grab_cooldown");
    }

    public boolean triggerHeld()
    {
        return triggerHeld;
    }
}
