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
import com.neep.meatweapons.entity.BulletDamageSource;
import com.neep.meatweapons.entity.HitScan;
import com.neep.meatweapons.entity.MeatgunProjectileEntity;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.particle.MuzzleFlashParticleType;
import com.neep.neepmeat.util.NMMaths;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;

public abstract class ShooterModule extends AmmunitionUsingModule
{
    protected final int amountPerShot;
    protected final int maxCooldown;

    protected int cooldown;

    protected final RandomSource shotRandom = RandomSource.create();

    public ShooterModule(RootModuleHolder.Listener listener, AmmunitionType type)
    {
        super(listener, type);
        this.amountPerShot = (int) getType().attributes().get(Attribute.AMOUNT_PER_USE);
        this.maxCooldown = (int) getType().attributes().get(Attribute.COOLDOWN);

        this.cooldown = 0;
    }

    public Vec3 getMuzzleOffset(LivingEntity entity, ItemStack stack, InteractionHand hand)
    {
        float baseOffset = listener.getHolder().getMeatgun().getWorldHeightOffset();
        boolean ads = AdsCounter.isADS(entity, stack, hand);
        return new Vec3(
                // XNOR
                ads ? 0 : ((hand == InteractionHand.MAIN_HAND) == (entity.getMainArm() == HumanoidArm.RIGHT)) ? -0.13 : 0.13,
                ads ? 0 : baseOffset,
                .25);
    }

    public static ShotOrigin getFireOrigin(LivingEntity entity, RandomSource shotRandom, float pitch, float yaw, float deviation)
    {
        Vector3f o = new Vector3f(0, 0, 1)
                .rotateY((float) shotRandom.triangle(0, deviation))
                .rotateX((float) shotRandom.triangle(0, deviation))
                .rotateX(pitch)
                .rotateY(-yaw);

        Vec3 pos = entity.getEyePosition();
        return new ShotOrigin(pos, new Vec3(o));
    }

    protected ShotOrigin getFireOrigin(LivingEntity entity, ItemStack stack, double pitch, double yaw)
    {
        InteractionHand hand = entity.getMainHandItem().equals(stack) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

        float deviation = getDeviation(entity, stack, hand);

        Vector3f o = new Vector3f(0, 0, 1)
                .rotateY((float) shotRandom.triangle(0, deviation))
                .rotateX((float) shotRandom.triangle(0, deviation))
                .rotateX((float) pitch)
                .rotateY((float) -yaw)
                ;

        Vec3 pos = entity.getEyePosition();
        Vec3 transform = getMuzzleOffset(entity, stack, hand).xRot((float) -pitch).yRot((float) -yaw);
        pos = pos.add(transform);

        return new ShotOrigin(pos, new Vec3(o));
    }

    protected float getAttackDamage()
    {
        return getType().attributes().attackDamage() / getOverclock();
    }

    protected int getMaxCooldown()
    {
        return (int) Math.max(2, maxCooldown / getOverclock());
    }

    protected float getOverclock()
    {
        return 1;
    }
    
    protected float getPrecisionModifier()
    {
        var types = listener.getHolder().moduleCache().moduleTypes();

        if (types.contains(MeatgunModules.BASIC_STOCK))
        {
            return 0.5f;
        }

        return 1;
    }

    protected void applyDamageEffects(Level world, LivingEntity shooter, HitScan.HitScanResult result)
    {
        Entity entity = result.getEntity();
        float damage = getAttackDamage();
        float f = MeatgunProjectileEntity.modifyDamage(world, damage, result, result.getDirection());
        boolean crit = f > 1;
        damage = f * damage;

        if (!(entity instanceof LivingEntity living && living.isDeadOrDying()))
        {
            boolean damaged = entity.hurt(BulletDamageSource.create(world, shooter, 0.1f, crit), damage);
            entity.invulnerableTime = 0; // ?

            if (damaged && listener.getHolder().moduleCache().moduleTypes().contains(MeatgunModules.BONE_STEALER))
            {
                BoneStealerModule.apply(shooter, entity, damage);
            }
        }
    }

    protected float getDeviation(LivingEntity entity, ItemStack stack, InteractionHand hand)
    {
        return (AdsCounter.isADS(entity, stack, hand) ? 4 : 8)
                * getPrecisionModifier()
                * Mth.DEG_TO_RAD;
    }

    protected abstract void fireWithModifiers(Level world, Player shooter, ItemStack stack, double pitch, double yaw, InteractionHand hand);

    public boolean allowedToUse(LivingEntity shooter)
    {
        return (!(shooter instanceof Player playerEntity && playerEntity.isSpectator()));
    }

    protected void fireEntity(Level world, LivingEntity shooter, ShotOrigin origin, InteractionHand hand) {}
    protected void fireBeam(Level world, LivingEntity entity, ShotOrigin origin, InteractionHand hand) {}

    public record ShotOrigin(Vec3 pos, Vec3 direction) {}

    protected void spawnMuzzleFlash(Level world, LivingEntity shooter, ParticleType<MuzzleFlashParticleType.MuzzleFlashParticleEffect> type, Vec3 pos, float ox, float oy, float oz, float scale, InteractionHand hand)
    {
        if (world instanceof ServerLevel serverWorld)
        {
            Vector4d v = new Vector4d(ox, oy, oz, 1);
            v.mul(this.transform.globalTransform);
            serverWorld.sendParticles(
                    new MuzzleFlashParticleType.MuzzleFlashParticleEffect(type, shooter, v.x, v.y, v.z, scale, 1, hand)
                    , pos.x(), pos.y(), pos.z(),
                    1, 0, 0, 0, 0.1);
        }
    }

    @Override
    protected boolean consume(Container inventory, Player player, InteractionHand hand)
    {
        cooldown = 10; // Minimal cooldown for reloading

        // Adjust for overclock
        int amountPerShot1 = Math.round(amountPerShot / getOverclock());
        return consume(amountPerShot1, inventory, player, hand);
    }
}
