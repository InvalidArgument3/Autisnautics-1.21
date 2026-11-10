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

package com.neep.meatweapons.item;

import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.item.MeatlibItemSettings;
import com.neep.meatweapons.MWItems;
import com.neep.meatweapons.Util;
import com.neep.meatweapons.client.model.FusionCannonItemModel;
import com.neep.meatweapons.entity.ExplodingShellEntity;
import com.neep.meatweapons.entity.FusionBlastEntity;
import com.neep.meatweapons.entity.WeaponCooldownAttachment;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.meatweapons.particle.MWGraphicsEffects;
import com.neep.neepmeat.init.NMSounds;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.model.GeoModel;

import java.util.Optional;

public class FusionCannonItem extends BaseGunItem implements WeakTwoHanded, Aimable
{
    public FusionCannonItem()
    {
        super(MWItems.SMALL_BALLISTIC_MAGAZINE, 16, 10, false, new MeatlibItemSettings());
        this.sounds.put(GunSounds.FIRE_PRIMARY, NMSounds.FUSION_FIRE.get());
        this.sounds.put(GunSounds.RELOAD, NMSounds.RELOAD.get());
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 70000;
    }

    protected static final String KEY_CHARGE = "charge";

    @Override
    public void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        WeaponCooldownAttachment manager = WeaponCooldownAttachment.get(player);
        if (id == MWAttackC2SPacket.TRIGGER_PRIMARY)
        {
            if (!manager.isCoolingDown(stack, 0))
            {
                if (stack.getDamageValue() != this.maxShots)
                {
                    // Primary trigger
                    manager.set(stack, 0, cooldown);
                    fireBeam(world, player, stack, pitch, yaw);
                    if (!player.isCreative()) stack.setDamageValue(stack.getDamageValue() + 1);
                }
            }

        }
        else if (id == MWAttackC2SPacket.TRIGGER_SECONDARY)
        {
            if (stack.getDamageValue() + 2 > stack.getMaxDamage())
            {
                this.reload(player, stack, null);
                return;
            }

            // Start charging secondary attack
            CompoundTag nbt = stack.getOrCreateTagElement(KEY_CHARGE);
            nbt.putInt("charge", 0);
            nbt.putBoolean("charging", true);

            world.playSound(null, player, NMSounds.FUSION_BLAST_CHARGE.get(), SoundSource.PLAYERS, 1f, 1f);
        }

        if (!manager.isCoolingDown(stack, 0) && stack.getDamageValue() >= this.maxShots)
        {
            this.reload(player, stack, null);
        }
    }

    @Override
    public void triggerClient(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType)
    {
        WeaponCooldownAttachment manager = WeaponCooldownAttachment.get(player);
        if (id == MWAttackC2SPacket.TRIGGER_PRIMARY)
        {
            if (!manager.isCoolingDown(stack, 0))
            {
                if (stack.getDamageValue() != this.maxShots)
                {
//                    syncAnimation(world, player, stack, "fire", true);
                }
            }

        }
    }

    @Override
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        // Continuously try to use the primary attack
        if (id == MWAttackC2SPacket.TRIGGER_PRIMARY)
        {
            trigger(world, player, stack, id, pitch, yaw, handType, hand);
        }

        // Increase charge in the secondary attack
        if (id == MWAttackC2SPacket.TRIGGER_SECONDARY)
        {
            CompoundTag nbt = stack.getOrCreateTagElement(KEY_CHARGE);
            if (nbt.getBoolean("charging"))
            {
                int charge = nbt.getInt("charge");
                nbt.putInt("charge", charge + 1);

                // Play a beep every second to indicate charge.
                if (charge != 0 && charge % 20 == 0)
                    world.playSound(null, player, NMSounds.MARATHON_BEEP.get(), SoundSource.PLAYERS, 1f, 1f + (float) charge / EXPLOSION_TIME);

                // Damage the player if they hold for too long.
                if (charge > EXPLOSION_TIME)
                {
                    world.explode(player, null, ExplodingShellEntity.KEEP_BEHAVIOUR, player.getX(), player.getY(), player.getZ(), 1, false, Level.ExplosionInteraction.MOB);
                    world.playSound(null, player, NMSounds.FUSION_BLAST_FIRE.get(), SoundSource.PLAYERS, 1f, 1f);
                    nbt.putInt("charge", 0);
                    nbt.putBoolean("charging", false);
                }
            }
        }
    }

    @Override
    public void release(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        // Check that the charge exceeds a threshold. If so, shoot the projectile.
        WeaponCooldownAttachment manager = WeaponCooldownAttachment.get(player);
        if (id == MWAttackC2SPacket.TRIGGER_SECONDARY)
        {
            if (stack.getDamageValue() + 2 <= maxShots
                    && canFireBlast(stack))
            {
                CompoundTag nbt = stack.getOrCreateTagElement(KEY_CHARGE);
                int charge = nbt.getInt("charge");
                float power = charge;

                fireShell(world, player, stack, 2, (world1, x, y, z, vx, vy, vz) -> new FusionBlastEntity(world1, x, y, z, vx, vy, vz, power));
                if (!player.isCreative()) stack.setDamageValue(stack.getDamageValue() + 2);

                world.playSound(null, player, NMSounds.FUSION_BLAST_FIRE.get(), SoundSource.PLAYERS, 1f, 1f);

                nbt.putBoolean("charging", false);
            }
            else
            {
                world.playSound(null, player, MeatlibSounds.CLICK, SoundSource.PLAYERS, 1f, 1f);
            }
        }
    }

    @Override
    protected void fireShell(Level world, Player player, ItemStack stack, double speed, ProjectileFactory factory)
    {
        Vec3 vec = Vec3.directionFromRotation(player.getXRot(), player.getYHeadRot()).scale(speed);

        Vec3 pos = new Vec3(player.getX(), player.getY() + 1.4, player.getZ());

        AbstractArrow shell = factory.create(world, pos.x, pos.y, pos.z, vec.x, vec.y, vec.z);
        shell.setOwner(player);
        world.addFreshEntity(shell);

        playSound(world, player, GunSounds.FIRE_SECONDARY);

        syncAnimation(world, player, stack, "fire", true);
    }

    @Override
    protected GeoModel<? extends BaseGunItem> createModel()
    {
        return new FusionCannonItemModel();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected)
    {
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    public static final int MAX_CHARGE_TIME = 60; // 60 ticks
    public static final int MIN_CHARGE_TIME = 13;
    public static final int EXPLOSION_TIME = 150; // Number of ticks before the weapon explodes when charging

    protected static boolean canFireBlast(ItemStack stack)
    {
        return stack.getOrCreateTagElement(KEY_CHARGE).getInt("charge") > MIN_CHARGE_TIME;
    }


    @Override
    public Vector3f getAimOffset()
    {
        return new Vector3f(0.56f, 0, 0);
    }

    @Override
    public Vec3 getMuzzleOffset(LivingEntity player, ItemStack stack)
    {
        boolean sneak = player.isShiftKeyDown();
        return new Vec3(
                sneak ? 0 : player.getMainHandItem().equals(stack) == (player.getMainArm() == HumanoidArm.RIGHT) ? -0.2 : 0.2,
                sneak ? -0.25 : 0.1,
                .5);
    }

    public void fireBeam(Level world, LivingEntity entity, ItemStack stack, double pitch, double yaw)
    {
        // Get projectile starting position and direction.
        Vec3 pos = new Vec3(entity.getX(), entity.getEyeY() - 0.2, entity.getZ());
//        Vec3d pos = new Vec3d(entity.getX(), entity.getY() + 1.4, entity.getZ());
        Vec3 transform = getMuzzleOffset(entity, stack).xRot((float) -pitch).yRot((float) -yaw);
        pos = pos.add(transform);

        Vec3 end = pos.add(Util.getRotationVector((float) pitch, (float) yaw).scale(20));
        Optional<EntityHitResult> target = hitScan(entity, pos, end, 20, this);
        DamageSource damage = entity instanceof Player player ?
                world.damageSources().playerAttack(player) : world.damageSources().mobAttack(entity);
        target.ifPresent(livingEntity -> livingEntity.getEntity().hurt(damage, 4));

        // Play fire sound
        playSound(world, entity, GunSounds.FIRE_PRIMARY);

        syncAnimation(world, entity, stack, "fire", true);
    }

    public void fireBeam(Level world, LivingEntity entity, Entity target, ItemStack stack)
    {
        // Get projectile starting position and direction.
        Vec3 pos = entity.getEyePosition();
        Vec3 end = target.position().add(0, target.getBbHeight() / 2, 0);

        Optional<EntityHitResult> foundTarget = hitScan(entity, pos, end, 20, this);
        DamageSource damage = entity instanceof Player player ?
                world.damageSources().playerAttack(player) : world.damageSources().mobAttack(entity);
        foundTarget.ifPresent(livingEntity -> livingEntity.getEntity().hurt(damage, 4));

        // Play fire sound
        playSound(world, entity, GunSounds.FIRE_PRIMARY);

        syncAnimation(world, entity, stack, "fire", true);

    }

    @Override
    public void syncBeamEffect(ServerLevel world, Vec3 pos, Vec3 end, float width, int maxTime, double showRadius, boolean full)
    {
        Vec3 col = new Vec3(214, 175, 32);
        for (ServerPlayer player : PlayerLookup.around(world, pos, showRadius))
        {
            MWGraphicsEffects.syncBeamEffect(player, MWGraphicsEffects.BEAM, world, pos, end, col, 0.5f, 5);
//            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, packet);
        }
    }

//    @Override
//    public void onAnimationSync(int id, int state)
//    {
//        final AnimationController<?> controller = GeckoLibUtil.getControllerForID(this.factory, id, controllerName);
//        if (state == ANIM_FIRE)
//        {
//            controller.markNeedsReload();
//            controller.setAnimation(new AnimationBuilder().addAnimation("animation.fusion.fire"));
//        }
//        else if (state == ANIM_RELOAD)
//        {
//            controller.markNeedsReload();
//            controller.setAnimation(new AnimationBuilder().addAnimation("animation.fusion.reload_r"));
//        }
//    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, controllerName, 0, this::fireController)
                .triggerableAnim("fire", RawAnimation.begin().thenPlay("animation.fusion.fire")));
    }
}
