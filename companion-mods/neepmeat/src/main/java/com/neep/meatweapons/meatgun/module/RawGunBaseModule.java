/*
 * Copyright (c) MeatWheeze 2026.
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

import com.neep.meatweapons.meatgun.RootModuleHolder;
import net.minecraft.nbt.CompoundTag;

public class RawGunBaseModule extends BasePistolModule
{
    public RawGunBaseModule(RootModuleHolder.Listener listener)
    {
        super(listener);
    }

    public RawGunBaseModule(RootModuleHolder.Listener listener, CompoundTag nbt)
    {
        this(listener);
    }


    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.BASIC_PISTOL_BASE;
    }
}
