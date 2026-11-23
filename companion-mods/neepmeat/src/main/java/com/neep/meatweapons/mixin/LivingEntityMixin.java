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

package com.neep.meatweapons.mixin;

import com.neep.meatweapons.entity.BulletDamageSource;
import com.neep.meatweapons.interfaces.HookableEntity;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements HookableEntity
{
    // Intercepts the custom bullet DamageSource in order to prevent silly amounts of knockback
    @Shadow protected int playerHitTimer;
    @Shadow protected Player attackingPlayer;

    @Unique @Nullable private Entity hookParent;
    @Unique private int hookTicks;
    @Unique private Vec3 initialOffset = Vec3.ZERO;

    public LivingEntityMixin(EntityType<?> type, Level world)
    {
        super(type, world);
    }

    @Shadow public abstract void setAttacker(@Nullable LivingEntity attacker);

    @Shadow public abstract boolean blockedByShield(DamageSource source);

    @Shadow public abstract void damageShield(float amount);

    @Shadow public abstract ItemStack getStackInHand(InteractionHand hand);

    @Shadow @Nullable private LivingEntity attacker;

    @Shadow protected abstract void applyDamage(DamageSource source, float amount);

    @Shadow protected float lastDamageTaken;

    @Shadow public abstract boolean isDead();

    @Shadow protected abstract boolean tryUseTotem(DamageSource source);

    @Shadow @Nullable protected abstract SoundEvent getDeathSound();

    @Shadow public abstract void onDeath(DamageSource source);

    @Shadow protected abstract float getSoundVolume();

    @Shadow public abstract float getSoundPitch();

    @Shadow protected abstract void playHurtSound(DamageSource source);

    @Shadow protected abstract void checkFallDamage(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition);

    @Shadow public int hurtTime;

    @Shadow public int maxHurtTime;

    @Shadow public abstract void setItemSlot(EquipmentSlot slot, ItemStack stack);

    @Shadow protected abstract void defineSynchedData();

    @Shadow protected abstract Vec3 getAttackPos();

    @Shadow
    public float bodyYaw;

    @Shadow
    private long lastDamageTime;

    @Shadow
    private @Nullable DamageSource lastDamageSource;

    @Inject(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
    {
        // This is supposed to mirror the implementation of LivingEntity::damage(), but gives control over knockback.
        // Without this, the minimum knockback an entity would receive from a bullet would be too large.
        if (source instanceof BulletDamageSource bulletSource)
        {
//            cir.setReturnValue(false);
//            if (true) return;

            LivingEntity thisEntity = ((LivingEntity) (Object) this);

            // I don't understand what's going on anymore
//            if (((Entity) (Object) this).timeUntilRegen > 10.0f)
//            {
//                if (amount <= this.lastDamageTaken)
//                {
//                    cir.setReturnValue(false);
//                    return;
//                }
//            }

            // Do shield things
            boolean shielded = false;
            float shieldedDamage = 0;
            if (amount > 0.0f && blockedByShield(source))
            {
                damageShield(amount);
                shieldedDamage = amount;
                shielded = true;
            }

            // Apply reduction based on the bullet's properties
            // In vanilla, reduction will occur if timeUntilRegen > 10
            // This equates to a cooldown of 10 ticks (20 - 10 = 10)
            if ((float)this.invulnerableTime > (20 - bulletSource.getInvulnerableCooldown()))
            {
                if (amount <= this.lastDamageTaken)
                {
                    cir.setReturnValue(false);
                    return;
                }

                this.applyDamage(source, amount - this.lastDamageTaken);
                this.lastDamageTaken = amount;
            }
            else
            {
                applyDamage(source, amount);
                lastDamageTaken = amount;
                maxHurtTime = 10;
                hurtTime = maxHurtTime;
                this.invulnerableTime = bulletSource.getRegenTime();
            }

            Entity sourceEntity = bulletSource.getEntity();
            if (sourceEntity instanceof LivingEntity livingAttacker)
            {
                setAttacker(livingAttacker);
                if (livingAttacker instanceof Player player)
                {
                    playerHitTimer = 100;
                    attackingPlayer = player;
                }
            }

            if (amount > 0)
            {
                lastDamageSource = source;
                lastDamageTime = level().getGameTime();
            }

            // Not entirely sure what this does
            sendStatus(thisEntity, shielded);

            this.level().broadcastDamageEvent(thisEntity, source);

            // Apply knockback
            if (sourceEntity != null)
                knockback(thisEntity, bulletSource, sourceEntity);

            playSounds(thisEntity, bulletSource);

            // Update stats
            updateStats(thisEntity, source, amount, shieldedDamage, shielded);

            cir.setReturnValue(true);
        }
    }

    @Unique
    protected void sendStatus(LivingEntity thisEntity, boolean shielded)
    {
        if (shielded)
        {
            // Shield bonk?
            thisEntity.getCommandSenderWorld().broadcastEntityEvent(thisEntity, (byte) 29);
        }
//        else if (source.isThorns())
//        {
//            // Thorns or something
//            thisEntity.getEntityWorld().sendEntityStatus(thisEntity, (byte) 33);
//        }
//        else
        {
            // Generic damage?
            thisEntity.getCommandSenderWorld().broadcastEntityEvent(thisEntity, (byte) 2);
        }
    }

    @Unique
    protected void updateStats(LivingEntity thisEntity, DamageSource source, float amount, float shieldedDamage, boolean shielded)
    {
        if (thisEntity instanceof ServerPlayer)
        {
            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer) thisEntity , source, amount, amount, shielded);
            if (shieldedDamage > 0.0f && shieldedDamage < 3.4028235E37f)
            {
                ((ServerPlayer) thisEntity).awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(shieldedDamage * 10.0f));
            }
        }
        if (attacker instanceof ServerPlayer) {
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer) attacker, thisEntity, source, amount, amount, shielded);
        }
    }

    @Unique
    protected void knockback(LivingEntity thisEntity, BulletDamageSource bulletSource, Entity attacker)
    {
        double dx = attacker.getX() - thisEntity.getX();
        double dz = attacker.getZ() - thisEntity.getZ();
        while (dx * dx + dz * dz < 1.0E-4)
        {
            dx = (Math.random() - Math.random()) * 0.01;
            dz = (Math.random() - Math.random()) * 0.01;
        }

        // Not sure what this was for
//        float knockback = (float)(MathHelper.atan2(dz, dx) * 57.2957763671875 - (double) thisEntity.getYaw());
        thisEntity.knockback(bulletSource.getPunch(), dx, dz);
    }

    @Unique
    protected void playSounds(LivingEntity thisEntity, BulletDamageSource source)
    {
        boolean usedTotem = false;
        if (isDead())
            usedTotem = tryUseTotem(source);

        BulletDamageSource.playHitDeathSounds(thisEntity, this::playHurtSound, getDeathSound(), getSoundVolume(), usedTotem, source);

        if (isDead() && !usedTotem)
            onDeath(source);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci)
    {
        this.hookTicks = Math.max(0, hookTicks - 1);
        if (hookTicks == 0)
            this.hookParent = null;

        if (hookParent != null)
        {
            Vec3 target = hookParent.position().add(initialOffset);
            Vec3 pos = position();
            push(
                    target.x - pos.x,
                    target.y - pos.y,
                    target.z - pos.z
            );
        }
    }

    @Override
    public void meatweapons$setHookParent(@Nullable Entity entity, Vec3 initialOffset)
    {
        this.hookParent = entity;
        this.initialOffset = initialOffset;
    }

    @Override
    public void meatweapons$setHookTicks(int ticks)
    {
        this.hookTicks = ticks;
    }
}
