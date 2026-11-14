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

import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.entity.MeatgunProjectileEntity;
import com.neep.meatweapons.entity.ShockStaffProjectileEntity;
import com.neep.meatweapons.item.BaseGunItem;
import com.neep.meatweapons.item.BeamEffectProvider;
import com.neep.meatweapons.meatgun.AmmunitionTransaction;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.meatweapons.network.MeatgunNetwork;
import com.neep.meatweapons.particle.MWParticles;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.util.NMMaths;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ShockStaffModule extends MeleeModule implements AmmunitionRequiringModule, AmmunitionStoringModule
{
    private int swingDownCooldown;
    private int ammoAmount;

    public ShockStaffModule(RootModuleHolder.Listener listener)
    {
        super(listener, List.of());
    }

    public ShockStaffModule(RootModuleHolder.Listener listener, CompoundTag nbt)
    {
        this(listener);
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.SHOCK_STAFF;
    }

    @Override
    public void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        if (id == 2 && swingDownCooldown == 0)
        {
            AmmunitionTransaction consume = consume(2, player.getInventory(), player, hand);
            if (consume.notEmpty())
            {
                MeatgunNetwork.SEND_ANIMATION.emitter(player).apply("swing_across", hand, null);
                world.playSound(null, player, NMSounds.SHOCK_STAFF_ATTACK.get(), SoundSource.PLAYERS, 1, 1);
                MeatgunNetwork.sendRecoil((ServerPlayer) player, 7, 0.3f,0.7f, 0.02f, hand);
                if (fireBeam(world, player, pitch, yaw, 3, hand))
                    consume.commit();

                swingDownCooldown = 15;
            }
        }
        else if (id == 1 && swingDownCooldown == 0)
        {
            AmmunitionTransaction shootConsume = consume(1, player.getInventory(), player, hand);
            if (shootConsume.notEmpty())
            {
                shootConsume.commit();
                MeatgunNetwork.SEND_ANIMATION.emitter(player).apply("swing_across", hand, null);
                world.playSound(null, player, NMSounds.SHOCK_STAFF_ATTACK.get(), SoundSource.PLAYERS, 1, 1);

                ShockStaffProjectileEntity entity = MeatWeapons.SHOCK_STAFF_PROJECTILE.create(world);
                Vec3 entityPos = player.getEyePosition();
                entity.setOwner(player);
                entity.setBaseDamage(baseDamage * 0.5f);
                entity.setPosRaw(entityPos.x, entityPos.y, entityPos.z);
                entity.setPos(entityPos.x, entityPos.y, entityPos.z);
                entity.shootFromRotation(player, (float) Math.toDegrees(pitch), (float) Math.toDegrees(yaw), 0, 0.9f, 0);
                MeatgunProjectileEntity.setupProjectileAttributes(entity, listener.getHolder(), 3);
                entity.setHomingSpeed(0.45f, true);
                world.addFreshEntity(entity);
                swingDownCooldown = 15;
            }
            else
            {
                shootConsume.commit();
            }
        }
    }

    @Override
    public void release(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
    }

    @Override
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        super.tickTrigger(world, player, stack, id, pitch, yaw, handType, hand);
    }

    @Override
    public void tick(Player player)
    {
        swingDownCooldown = Math.max(0, swingDownCooldown - 1);
//        projectileCooldown = Math.max(0, projectileCooldown - 1);
    }

    @Override
    protected boolean fireBeam(Level world, Player player, double pitch, double yaw, double range, InteractionHand hand)
    {
        Vec3 pos = player.getEyePosition();

        Vec3 end = pos.add(NMMaths.getRotationVector(pitch, yaw).scale(range + 1));
        @Nullable EntityHitResult target = BaseGunItem.hitScan(player, pos, end, range, e -> e != player.getVehicle(),
                BeamEffectProvider.NONE, 0.4f).orElse(null);

        MeatgunNetwork.sendRecoil(player, 4, 0.2f, 0.7f, 0.03f, hand);

        if (target != null && !target.getEntity().hasPassenger(player) && target.getEntity().isAttackable())
        {
            if (meleeDamageEntity(world, player, target.getEntity()))
            {
                Vec3 targetPos = target.getLocation();
                world.playSound(null, player, NMSounds.SHOCK_STAFF_HIT.get(), SoundSource.PLAYERS, 1, 1);
                ((ServerLevel) world).sendParticles(MWParticles.SHOCK_STAFF, targetPos.x, targetPos.y, targetPos.z, 10, 0.05, 0.05, 0.05, 0.03);
            }

            return true;
        }
        return false;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        super.writeNbt(nbt);
        nbt.putInt("ammo_amount", ammoAmount);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt, @Nullable Matrix4f global)
    {
        super.readNbt(nbt, global);
        this.ammoAmount = nbt.getInt("ammo_amount");
    }

    @Override
    public int capacity()
    {
        return 16;
    }

    @Override
    public AmmunitionType ammoType()
    {
        return AmmunitionType.ENERGY;
    }

    @Override
    public int amount()
    {
        return ammoAmount;
    }

    @Override
    public int insert(int maxAmount)
    {
        int inserted = Math.min(maxAmount, capacity() - ammoAmount);
        if (inserted > 0)
        {
            ammoAmount += inserted;
        }
        return inserted;
    }

    @Override
    public int extract(int maxAmount)
    {
        int extracted = Math.min(ammoAmount, maxAmount);
        if (extracted > 0)
        {
            ammoAmount -= extracted;
            return extracted;
        }
        return 0;
    }

    public AmmunitionTransaction consume(int amount, Container inventory, Player player, InteractionHand hand)
    {
        this.swingDownCooldown = 10;
        return listener.getHolder().getAmmoOrReload(ammoType(), amount, inventory, player, hand);
    }
}
