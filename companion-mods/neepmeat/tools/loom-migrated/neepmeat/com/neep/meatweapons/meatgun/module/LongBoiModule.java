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

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.network.GlobalChannelManager;
import com.neep.meatlib.util.PlayerVelocity;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.component.AdsCounter;
import com.neep.meatweapons.damage.MWDamageSources;
import com.neep.meatweapons.entity.BulletDamageSource;
import com.neep.meatweapons.entity.HitScan;
import com.neep.meatweapons.entity.MeatgunProjectileEntity;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.meatweapons.network.MeatgunNetwork;
import com.neep.meatweapons.particle.MWGraphicsEffects;
import com.neep.meatweapons.particle.MWParticles;
import com.neep.neepmeat.init.NMSounds;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LongBoiModule extends ShooterModule
{
    public static final GlobalChannelManager<StartSound> SYNC_SOUND = GlobalChannelManager.create(ResourceLocation.tryBuild(MeatWeapons.NAMESPACE, "long_boi_sound"),
            StartSound.FORMAT);

    private final RandomSource shotRandom = RandomSource.create();

    private boolean triggered = false;
    private int charge; // 0-130
    private final int maxCharge = 130;

    public LongBoiModule(RootModuleHolder.Listener listener)
    {
        super(listener, AmmunitionType.BALLISTIC);
    }

    public LongBoiModule(RootModuleHolder.Listener listener, CompoundTag nbt)
    {
        this(listener);
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.LONG_BOI;
    }

    @Override
    public void tick(Player player)
    {
        cooldown = Math.max(0, cooldown - 1);
    }

    @Override
    public void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        if (!triggered && cooldown == 0)
        {
            triggered = true;
            startSound(player);
        }
        else if (triggered)
        {
            charge = Mth.clamp(charge + 1, 0, maxCharge);
        }
    }

    private void playSound(Player playerEntity, SoundEvent sound, float volume, float pitch)
    {
        playerEntity.level().playSound(null, playerEntity, sound, SoundSource.PLAYERS, volume, pitch);
    }

    @Override
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        trigger(world, player, stack, id, pitch, yaw, handType, hand);

//        if (charge != 0 && charge % 20 == 0 && charge != maxCharge)
//            playSound(player, NMSounds.MARATHON_BEEP, 1f, 1f + (float) charge / 100);

        if (charge == 40 || charge == 44)
            playSound(player, MeatlibSounds.UI_BEEP, 1f, 0.7f);

        if (charge % 5 == 0)
        {
            float x = charge / (float) maxCharge;
            MeatgunNetwork.sendShake(player, 0.1f * x * x, 10, hand);
        }

        if (charge % 10 == 0)
        {
            if (!consume(1, player.getInventory(), player, hand))
            {
                stopSound(player);
                charge = 0;
                triggered = false;
            }
        }

        if (charge >= 130)
        {
            // Die
            world.explode(null, player.damageSources().source(MWDamageSources.MEATGUN_EXPLOSION, player),
                    null, player.getX(), player.getY(), player.getZ(),
                    2.1f, false, Level.ExplosionInteraction.MOB);
            charge = 0;
            triggered = false;
            stopSound(player);
        }
        else if (charge >= 80)
        {
            // Warning
            if ((charge % 40) == 0)
                playSound(player, NMSounds.DOSIMETER_ALARM, 1, 1);
        }
        else if (charge >= 75)
        {
            // Start enlightening
        }
        else if (charge >= 40)
        {
            // Indicate beam
        }
    }

    @Override
    protected boolean consume(Container inventory, Player player, InteractionHand hand)
    {
        return consume(amountPerShot, inventory, player, hand);
    }

    protected boolean consume(int amount, Container inventory, Player player, InteractionHand hand)
    {
        var consume = listener.getHolder().getAmmoOrReload(ammoType(), amount, inventory, player, hand);

        if (consume.notEmpty())
        {
            consume.commit();
            return true;
        }

        // We don't want to interrupt firing if a reload occurred.
        // If it failed, try again. A reload might have occurred.
        var consume2 = listener.getHolder().getAmmoOrReload(ammoType(), amount, inventory, player, hand);

        if (consume2.notEmpty())
        {
            consume2.commit();
            return true;
        }

        return false;
    }

    @Override
    public void release(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        var origin = getFireOrigin(player, stack, pitch, yaw);
        // Explosive beam
        if (charge >= 80)
        {
            fireExplosiveBeam(world, player, stack, origin, hand);
        }
        else if (charge >= 40)
        {
            // Beam
            fireNormalBeam(world, player, stack, origin, hand, getAttackDamage() + 10 * ((charge - 40f) / (maxCharge - 40f)));
        }
        else if (charge >= 10)
        {
            // Entity
//            fireEntity(world, player, origin, hand);
        }

        charge = 0;
        triggered = false;
        stopSound(player);
    }

    private void startSound(Player origin)
    {
        SYNC_SOUND.emitter(origin).apply(origin.getId(), true);
    }

    private void stopSound(Player origin)
    {
        SYNC_SOUND.emitter(origin).apply(origin.getId(), false);
    }

    @Override
    protected void fireWithModifiers(Level world, Player shooter, ItemStack stack, double pitch, double yaw, InteractionHand hand)
    {
    }

    @Override
    protected float getDeviation(LivingEntity entity, ItemStack stack, InteractionHand hand)
    {
        return 2 * Mth.DEG_TO_RAD;
    }

    protected void fireExplosiveBeam(Level world, LivingEntity shooter, ItemStack stack, ShotOrigin origin, InteractionHand hand)
    {
        float power = Mth.clamp((charge - 80) / (float) (maxCharge - 80), 0, 1);

        var result = fireNormalBeam(world, shooter, stack, origin, hand, getAttackDamage() + 10);
        var finalPos = result.finalPos();

        explosion(shooter, world, finalPos.x, finalPos.y, finalPos.z, 1 + 2 * power);

        Vec3 addedVel = origin.direction().scale(-3 * power);
        shooter.addDeltaMovement(addedVel);
//        shooter.velocityDirty = true;
//        shooter.velocityModified = true;
        if (shooter instanceof Player playerEntity)
            PlayerVelocity.CHANNEL.emitter(playerEntity).apply(addedVel, false);
    }

    private void explosion(LivingEntity source, Level world, double x, double y, double z, float power)
    {
        world.explode(null, source.damageSources().source(MWDamageSources.MEATGUN_EXPLOSION, source),
                null, x, y, z,
                1 + power, false, Level.ExplosionInteraction.MOB);
        world.addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1.0, 0.0, 0.0);
    }

    protected HitScan.Result fireNormalBeam(Level world, LivingEntity shooter, ItemStack stack, ShotOrigin origin, InteractionHand hand, float damage)
    {
        var pos = origin.pos();

        int range = 100;
        var result = new HitScan(range)
                .ricochets(0)
                .ricochetChance(0)
                .maxPierce(3)
                .margin(beamRadius() / 2f)
                .beamEffect(this::syncBeamEffect)
                .apply(world, shooter, origin, shotRandom);

        for (var e : result.hits())
        {
            float f = MeatgunProjectileEntity.modifyDamage(world, damage, e, e.getDirection());
            Entity entity = e.getEntity();
            entity.hurt(BulletDamageSource.create(world,
                    shooter,
                    Math.min(2, (float) f * charge / maxCharge * 3), false),
                    damage);
            entity.invulnerableTime = 0;

            spawnParticles(world, e.getLocation());
        }

        boolean ads = AdsCounter.isADS(shooter, stack, hand);
        MeatgunNetwork.sendRecoil(shooter,
                ads ? 5 : 10,
                ads ? 0.5f : 1.4f,
                0.7f, 0.03f, hand);
        world.playSound(null, shooter, NMSounds.LONG_BOI_FIRE, SoundSource.PLAYERS, 1f, 1f);

        spawnMuzzleFlash(world, shooter, MWParticles.LONG_BOI_MUZZLE_FLASH, pos, 0, 0, -34 / 16f, 1.9f, hand);

        return result;
    }

    private void spawnParticles(Level world, Vec3 pos)
    {
        if (world instanceof ServerLevel serverWorld)
        {
            serverWorld.sendParticles(MWParticles.LONG_BOI_HIT, pos.x, pos.y, pos.z, 10, 0.1, 0.1, 0.1, 0.4);
        }
    }

    public float beamRadius()
    {
        return 0.1f + 1.3f * ((charge - 40f) / (maxCharge - 40f));
    }

    public void syncBeamEffect(ServerLevel world, Vec3 pos, Vec3 end, float width, int maxTime, double showRadius, boolean full)
    {
        float radius = beamRadius();
        spawnParticles(world, end);
        Vec3 col = new Vec3(255, 255, 255);
        for (ServerPlayer player : PlayerLookup.around(world, pos, showRadius))
        {
            MWGraphicsEffects.syncBeamEffect(player, MWGraphicsEffects.LONG_BULLET_TRAIL, world, pos, end, col, radius, 40);
        }
    }

    public interface StartSound
    {
        ChannelFormat<StartSound> FORMAT = ChannelFormat.builder(StartSound.class)
                .param(ParamCodec.INT)
                .param(ParamCodec.BOOLEAN)
                .build();

        void apply(int entity, boolean start);
    }
}
