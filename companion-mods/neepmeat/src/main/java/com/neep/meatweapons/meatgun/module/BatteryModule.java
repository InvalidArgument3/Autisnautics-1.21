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

import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class BatteryModule extends AbstractMeatgunModule implements AmmunitionStoringModule
{
    private int ammoAmount;

    public BatteryModule(RootModuleHolder.Listener listener)
    {
        super(listener);
    }

    public BatteryModule(RootModuleHolder.Listener listener, CompoundTag nbt)
    {
        this(listener);
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.BATTERY;
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
}
