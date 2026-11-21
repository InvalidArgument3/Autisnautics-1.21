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
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.SlotType;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.meatweapons.network.MeatgunNetwork;
import com.neep.meatweapons.particle.MWGraphicsEffects;
import com.neep.meatweapons.particle.MWParticles;
import com.neep.neepmeat.init.NMSounds;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public class ChuggerModule extends ShooterModule
{
    private final ModuleSlot barrel;
    private final ModuleSlot firingMechanism;

    public ChuggerModule(RootModuleHolder.Listener listener)
    {
        super(listener, AmmunitionType.BALLISTIC);
        this.barrel = new SimpleModuleSlot(listener, new Matrix4f().translate(0, 0, -9 / 16f), SlotType.BARREL, true);
        this.firingMechanism = new SimpleModuleSlot(listener, new Matrix4f(), SlotType.FIRING_MECHANISM, false);
        setSlots(List.of(barrel, firingMechanism));
    }

    public ChuggerModule(RootModuleHolder.Listener listener, CompoundTag nbt)
    {
        this(listener);
        readNbt(nbt, new Matrix4f());
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.CHUGGER;
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
                cooldown = getMaxCooldown();

                fireWithModifiers(world, player, stack, pitch, yaw, hand);
            }
        }
    }

    protected float getOverclock()
    {
        float f = firingMechanism.get() instanceof FiringMechanismModule mechanism ? mechanism.getOverclock() : 1;
        float newCooldown = maxCooldown / f;
        newCooldown = Math.max(2, newCooldown);
        f = maxCooldown / newCooldown;
        return f;
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
        if (slots.get(0).get().getType() == MeatgunModules.SLOW_BARREL)
            fireEntity(world, shooter, origin, hand);
        else
            fireBeam(world, shooter, origin, hand);

        boolean ads = AdsCounter.isADS(shooter, stack, hand);
        MeatgunNetwork.sendRecoil(shooter,
                ads ? 3 : 7,
                0.2f,0.7f, 0.03f,
                2, hand);

        world.playSound(null, shooter, NMSounds.CHUGGER_FIRE.get(), SoundSource.PLAYERS, 0.8f, (float) shotRandom.triangle(0.8f, 0.2f));

        var pos = origin.pos();
        spawnMuzzleFlash(world, shooter, MWParticles.NORMAL_MUZZLE_FLASH, pos, 0, 0, -9 / 16f, 2.2f, hand);
    }

    @Override
    protected void fireEntity(Level world, LivingEntity shooter, ShotOrigin origin, InteractionHand hand)
    {
        float speed = 3f;
        var pos = origin.pos();
        var velocity = origin.direction().scale(speed);
        var entity = new BulletEntity(world, pos.x, pos.y, pos.z, velocity.x, velocity.y, velocity.z);
        entity.setNoGravity(false);

        entity.setHomingSpeed(speed / 4, true);
        entity.setOwner(shooter);
        entity.setMaxDistance(40);
        entity.setBaseDamage(getAttackDamage());
        entity.setBulletPunch(0.1f);
        entity.setMaxCollisions(shotRandom.nextFloat() < 0.25f ? 1 : 0, 1.5f);
        entity.setCol(0x50392c);
        MeatgunProjectileEntity.setupProjectileAttributes(entity, listener.getHolder(), 3);
        world.addFreshEntity(entity);
    }

    @Override
    protected void fireBeam(Level world, LivingEntity shooter, ShotOrigin origin, InteractionHand hand)
    {
        var result = new HitScan(40)
                .ricochets(2)
                .ricochetChance(0.25f)
                .maxPierce(0)
                .beamEffect(this::syncBeamEffect)
                .apply(world, shooter, origin, shotRandom);

        for (var e : result.hits())
        {
            applyDamageEffects(world, shooter, e);
        }

        if (result.ricochet())
            world.playSound(null, shooter, NMSounds.BULLET_RICOCHET.get(), SoundSource.PLAYERS, 1f, (float) shotRandom.triangle(1f, 0.4f));
    }

    public void syncBeamEffect(ServerLevel world, Vec3 pos, Vec3 end, float width, int maxTime, double showRadius, boolean full)
    {
        Vec3 col = new Vec3(154, 155, 32);
        for (ServerPlayer player : PlayerLookup.around(world, pos, showRadius))
        {
            MWGraphicsEffects.syncBeamEffect(player, full ? MWGraphicsEffects.RICOCHET_BULLET_TRAIL : MWGraphicsEffects.BULLET_TRAIL, world, pos, end, col, 0.1f, 1);
        }
    }
}
