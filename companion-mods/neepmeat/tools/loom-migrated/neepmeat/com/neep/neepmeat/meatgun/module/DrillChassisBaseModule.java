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

package com.neep.neepmeat.meatgun.module;

import com.neep.meatweapons.implant.AmmunitionGeneratorImplant;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.SlotType;
import com.neep.meatweapons.meatgun.module.*;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.neepmeat.meatgun.NMMeatgunModules;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DrillChassisBaseModule extends AbstractMeatgunModule implements AmmunitionStoringModule
{
    private final SimpleModuleSlot mainSlot;
    private int amount;

    public DrillChassisBaseModule(RootModuleHolder.Listener listener)
    {
        super(listener);

        mainSlot = new SimpleModuleSlot(listener, new Matrix4f()
                .translate(0, 0, -18 / 16f),
                SlotType.DRILL, true);

        var auxSlot1 = new SimpleModuleSlot(listener, new Matrix4f()
                .translate(0, 3 / 16f, -4 / 16f)
                .rotateX((float) Math.toRadians(90))
                .rotateY((float) Math.toRadians(-45))
                .translate(3 / 16f, 1f / 16f, 0)
        );

        var auxSlot2 = new SimpleModuleSlot(listener, new Matrix4f()
                .translate(0, 3 / 16f, -4 / 16f)
                .rotateX((float) Math.toRadians(90))
                .rotateY((float) Math.toRadians(-45))
                .translate(3 / 16f, -4f / 16f, 0)
        );

        setSlots(List.of(mainSlot, auxSlot1, auxSlot2));
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
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        mainSlot.get().tickTrigger(world, player, stack, id, pitch, yaw, handType, hand);
    }

    @Override
    public void tick(Player player)
    {
        super.tick(player);
        AmmunitionGeneratorImplant.tickGeneration(player, listener.getHolder());
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return NMMeatgunModules.BASE_ROCK_DRILL;
    }

    public static DrillChassisBaseModule fromNbt(RootModuleHolder.Listener listener, CompoundTag nbtCompound)
    {
        var m = new DrillChassisBaseModule(listener);
        m.readNbt(nbtCompound, null);
        return m;
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
//        if (!MeatLib.isClient())
//            System.out.println("read on server");

        super.readNbt(nbt, global);
        this.amount = nbt.getInt("amount");
    }

    public MeatgunModule getHead()
    {
        return mainSlot.get();
    }

    @Override
    public int capacity()
    {
        return 1024;
    }

    @Override
    public AmmunitionType ammoType()
    {
//        return listener.getHolder().coreImplants().fuelType();
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

    public ModuleSlot getHeadSlot()
    {
        return mainSlot;
    }
}
