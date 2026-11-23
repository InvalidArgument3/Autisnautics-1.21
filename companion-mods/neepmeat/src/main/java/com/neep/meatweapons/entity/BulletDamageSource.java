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

package com.neep.meatweapons.entity;

import com.neep.meatweapons.damage.MWDamageSources;
import com.neep.meatweapons.init.MWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class BulletDamageSource extends DamageSource
{
    private final float punch;
    private final int regenTime;
    private final boolean isCritical;

    // Time since a previous damage event after which this bullet can cause damage
    // Vanilla value is 10
    private final int invulnerableCooldown;

    public BulletDamageSource(Holder<DamageType> type, @Nullable Entity attacker, float punch, int regenTime, boolean critical, int cooldown)
    {
        super(type, null, attacker);
        this.punch = punch;
        this.regenTime = regenTime;
        this.isCritical = critical;
        this.invulnerableCooldown = cooldown;
    }

//    public BulletDamageSource(LivingEntity attacker, float punch)
//    {
//        this(attacker, punch, 20);
//    }

//    public BulletDamageSource(LivingEntity attacker, float punch, int regenTime)
//    {
//        this.attacker = attacker;
//        this.punch = punch;
//        this.regenTime = regenTime;
//    }

    public static BulletDamageSource create(Level world, LivingEntity attacker, float punch, boolean critical)
    {
        return new BulletDamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(MWDamageSources.BULLET), attacker, punch, 15, critical, 0);
    }

    public static BulletDamageSource create(Level world, LivingEntity attacker, float punch, int regenTime, boolean critical, int cooldown)
    {
        return new BulletDamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(MWDamageSources.BULLET), attacker, punch, regenTime, critical, cooldown);
    }

    public static void playHitDeathSounds(LivingEntity entity, Consumer<DamageSource> playHurtSound, SoundEvent deathSound, float sounVolume, boolean usedTotem, BulletDamageSource source)
    {
        boolean crit = source.isCritical();
        if (entity.isDeadOrDying())
        {
            if (!usedTotem)
            {
                if (deathSound != null)
                {
                    float volume = sounVolume / (crit ? 2f : 1f);
                    entity.playSound(deathSound, volume, entity.getVoicePitch());
                }

                if (crit)
                    entity.playSound(MWSounds.BULLET_HIT_LIVING_CRIT, 1, 1);

            }
        }
        else
        {
            if (crit)
                entity.playSound(MWSounds.BULLET_HIT_LIVING_CRIT, 1F,
                        (float) entity.getRandom().triangle(1, 0.2));
            else
                playHurtSound.accept(source);
        }
    }

    public float getPunch()
    {
        return punch;
    }

    public int getRegenTime()
    {
        return regenTime;
    }

    public boolean isCritical()
    {
        return isCritical;
    }

    public int getInvulnerableCooldown()
    {
        return invulnerableCooldown;
    }
}
