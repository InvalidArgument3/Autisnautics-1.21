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

import com.neep.meatweapons.item.BaseGunItem;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.network.MeatgunNetwork;
import com.neep.neepmeat.util.NMMaths;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class MeleeModule extends AbstractMeatgunModule
{
    protected float baseDamage;

    public MeleeModule(RootModuleHolder.Listener listener, List<ModuleSlot> slots)
    {
        super(listener, slots);
        this.baseDamage = getType().attributes().attackDamage();
    }

    protected boolean fireBeam(Level world, Player player, double pitch, double yaw, double range, InteractionHand hand)
    {
        Vec3 pos = player.getEyePosition();

        Vec3 end = pos.add(NMMaths.getRotationVector(pitch, yaw).scale(range + 1));
        @Nullable EntityHitResult target = BaseGunItem.hitScan(player, pos, end, range, e -> e != player.getVehicle(),
                (world1, pos1, end1, width, maxTime, showRadius, full) -> {}, 0.4f).orElse(null);

        MeatgunNetwork.sendRecoil(player, 7, 0.2f, 0.7f, 0.03f, hand);

        if (target != null && !target.getEntity().hasPassenger(player) && target.getEntity().isAttackable())
        {
            Entity targetEntity = target.getEntity();
            meleeDamageEntity(world, player, targetEntity);
            return true;
        }
        return false;
    }

    protected boolean meleeDamageEntity(Level world, Player player, Entity targetEntity)
    {
        if (!targetEntity.skipAttackInteraction(player))
        {
            boolean crit = canCrit(player);
            float playerDamageModifier = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);

            float damage = playerDamageModifier * baseDamage * (crit ? 1.5f : 1);
            targetEntity.hurt(world.damageSources().playerAttack(player), damage);

            if (crit)
            {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, player.getSoundSource(), 1.0F, 1.0F);
                player.crit(targetEntity);
            }

            if (listener.getHolder().moduleCache().moduleTypes().contains(MeatgunModules.BONE_STEALER))
            {
                BoneStealerModule.apply(player, targetEntity, damage);
            }

            return true;
        }

        return false;
    }

    protected boolean canCrit(Player player)
    {
        return  player.fallDistance > 0.0F
                && !player.onGround()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.hasEffect(MobEffects.BLINDNESS)
                && !player.isPassenger()
                && !player.isSprinting();
    }

    protected float getAttackDamage(Player player)
    {
        return (canCrit(player) ? 1 : 1.5f) * baseDamage;
    }
}
