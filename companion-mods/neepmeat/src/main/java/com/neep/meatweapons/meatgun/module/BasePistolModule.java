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
import com.neep.meatweapons.meatgun.SlotType;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class BasePistolModule extends AbstractMeatgunModule implements AmmunitionStoringModule
{
    private int amount;

    public BasePistolModule(RootModuleHolder.Listener listener)
    {
        super(listener);
        ModuleSlot front = new SimpleModuleSlot(this.listener, new Matrix4f());
        ModuleSlot back = new SimpleModuleSlot(this.listener, new Matrix4f().rotateY(Mth.PI).translate(0, 0, -7 / 16f),
                EnumSet.of(SlotType.NORMAL, SlotType.STOCK), true);
        setSlots(List.of(front, back));
    }

    public BasePistolModule(RootModuleHolder.Listener listener, CompoundTag nbt)
    {
        this(listener);
    }

    public ModuleSlot getFront()
    {
        return slots.get(0);
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.BASE_PISTOL;
    }

    public static BasePistolModule fromNbt(RootModuleHolder.Listener listener, CompoundTag nt)
    {
        return new BasePistolModule(listener, nt);
    }

    @Override
    public void tick(Player player)
    {
        super.tick(player);
        AmmunitionGeneratorImplant.tickGeneration(player, listener.getHolder());
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
}
