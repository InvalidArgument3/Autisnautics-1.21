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

import com.neep.meatweapons.entity.BounceGrenadeEntity;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.meatweapons.network.MeatgunNetwork;
import com.neep.meatweapons.particle.MWParticles;
import com.neep.neepmeat.init.NMSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GrenadeLauncherModule extends ShooterModule
{
    private final RandomSource shotRandom = RandomSource.create();

    public GrenadeLauncherModule(RootModuleHolder.Listener listener)
    {
        super(listener, AmmunitionType.BALLISTIC);
    }

    public GrenadeLauncherModule(RootModuleHolder.Listener listener, CompoundTag nbt)
    {
        this(listener);
        readNbt(nbt, null);
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.GRENADE_LAUNCHER;
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

                fireWithModifiers(world, player, stack, pitch, yaw, hand);
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
        var origin = getFireOrigin(shooter, stack, pitch, yaw);
        fireEntity(world, shooter, origin, hand);
    }

    @Override
    protected void fireEntity(Level world, LivingEntity shooter, ShotOrigin origin, InteractionHand hand)
    {
        Vec3 pos = origin.pos();
        float speed = 0.8f;

        BounceGrenadeEntity entity = new BounceGrenadeEntity(world, 2f, 40, false);
        entity.setPosRaw(pos.x, pos.y, pos.z);
        entity.setPos(pos.x, pos.y, pos.z);
        entity.setDeltaMovement(origin.direction().scale(speed));
        entity.setOwner(shooter);
        entity.setMaxDistance(60);
        entity.setMaxCollisions(4, 1.5f);

        if (listener.getHolder().containsType(MeatgunModules.HOMING_BRAIN))
        {
            entity.setHomingRadius(4);
            entity.setHomingSpeed(speed / 2f, false);
        }

        world.addFreshEntity(entity);

        MeatgunNetwork.sendRecoil(shooter, 7, 0.2f,0.7f, 0.03f, 6, hand);
        world.playSound(null,
                shooter,
                NMSounds.GRENADE_LAUNCHER_FIRE,
                SoundSource.PLAYERS,
                (float) shotRandom.triangle(0.7f, 0.2f),
                (float) shotRandom.triangle(1, 0.2f));
        spawnMuzzleFlash(world, shooter, MWParticles.BLOB_MUZZLE_FLASH, pos, 0, 0, -13 / 16f, 2.2f, hand);
    }
}
