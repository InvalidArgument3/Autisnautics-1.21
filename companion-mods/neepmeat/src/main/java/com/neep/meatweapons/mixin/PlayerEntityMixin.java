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

import com.mojang.datafixers.util.Either;
import com.neep.meatweapons.entity.HitOnCollideEntity;
import com.neep.meatweapons.entity.PlayerWeaponManager;
import com.neep.meatweapons.interfaces.MWPlayerEntity;
import com.neep.meatweapons.meatgun.module.HalberdModule;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends Entity implements MWPlayerEntity, HitOnCollideEntity
{
    @Shadow public abstract Either<Player.BedSleepingProblem, Unit> trySleep(BlockPos pos);

    @Shadow public abstract void remove(RemovalReason reason);

    @Shadow public abstract boolean checkFallFlying();

    @Unique private int collideHitTicks;
    @Unique private float collideHitDamage;
    @Unique private boolean stealBones;

    @Unique
    protected PlayerWeaponManager manager = new PlayerWeaponManager((Player) (Object) this);

    public PlayerEntityMixin(EntityType<?> type, Level world)
    {
        super(type, world);
    }

    @Override
    public PlayerWeaponManager meatweapons$getWeaponManager()
    {
        return manager;
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    void onTick(CallbackInfo ci)
    {
        manager.tick();

        collideHitTicks = Math.max(0, collideHitTicks - 1);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
    {
        // Invulnerability is necessary to make this attack useful.
        if (collideHitTicks > 0)
        {
            if (source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK))
            {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

    @Override
    public void meatweapons$startCollideHit(int ticks, float damage, boolean stealBones)
    {
        this.collideHitTicks = ticks;
        this.collideHitDamage = damage;
        this.stealBones = stealBones;
    }

    @Inject(method = "collideWithEntity", at = @At("HEAD"))
    private void onCollideWithEntity(Entity entity, CallbackInfo ci)
    {
        HalberdModule.onEntityCollide((Player) (Object) this, entity, collideHitTicks, collideHitDamage, stealBones);
    }
}
