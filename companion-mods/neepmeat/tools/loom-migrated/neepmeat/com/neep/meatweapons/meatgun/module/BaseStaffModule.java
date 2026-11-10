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

import com.neep.meatweapons.implant.AmmunitionGeneratorImplant;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BaseStaffModule extends AbstractMeatgunModule implements AmmunitionStoringModule
{
    private final SimpleModuleSlot mainSlot;

    private int amount;

    public BaseStaffModule(RootModuleHolder.Listener listener)
    {
        super(listener);

        mainSlot = new SimpleModuleSlot(listener, new Matrix4f()
                .rotateX((float) Math.toRadians(90))
                .translate(0, 0, -8 / 16f));

        var auxSlot = new SimpleModuleSlot(listener, new Matrix4f()
                .rotateX((float) Math.toRadians(90))
                .rotateY((float) Math.toRadians(-90))
                .rotateZ((float) Math.toRadians(90))
                .translate(0, 4 / 16f, -1 / 16f));

        setSlots(List.of(mainSlot, auxSlot));
    }

    @Override
    public void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        mainSlot.get().trigger(world, player, stack, id, pitch, yaw, handType, hand);
    }

    @Override
    public void release(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        mainSlot.get().release(world, player, stack, id, pitch, yaw, handType, hand);
    }

    @Override
    public void tick(Player player)
    {
        super.tick(player);
        AmmunitionGeneratorImplant.tickGeneration(player, listener.getHolder());
    }

    @Override
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        mainSlot.get().tickTrigger(world, player, stack, id, pitch, yaw, handType, hand);
    }

    @Override
    public int capacity()
    {
        return 512;
    }

    @Override
    public AmmunitionType ammoType()
    {
        return AmmunitionType.METABOLIC;
    }

    @Override
    public int amount()
    {
        return amount;
    }

    @Override
    public int insert(int maxAmount)
    {
        int inserted = Math.min(maxAmount, capacity() - amount);
        if (inserted > 0)
        {
            amount += inserted;
        }
        return inserted;
    }

    @Override
    public int extract(int maxAmount)
    {
        int extracted = Math.min(amount, maxAmount);
        if (extracted > 0)
        {
            amount -= extracted;
            return extracted;
        }
        return 0;
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.BASE_STAFF;
    }

    public static BaseStaffModule fromNbt(RootModuleHolder.Listener listener, CompoundTag nbtCompound)
    {
        return new BaseStaffModule(listener);
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        super.writeNbt(nbt);
        nbt.putInt("amount", amount);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt, @Nullable Matrix4f global)
    {
        super.readNbt(nbt, global);
        this.amount = nbt.getInt("amount");
    }
}
