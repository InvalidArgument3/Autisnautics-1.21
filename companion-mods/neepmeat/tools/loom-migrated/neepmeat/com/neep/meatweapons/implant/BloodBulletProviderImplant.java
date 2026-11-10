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

package com.neep.meatweapons.implant;

import com.neep.meatweapons.MeatWeapons;
import com.neep.neepmeat.implant.entity.EntityImplant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class BloodBulletProviderImplant implements EntityImplant
{
    public static final ResourceLocation ID = new ResourceLocation(MeatWeapons.NAMESPACE, "blood_bullet_provider");

    private final Entity entity;


    public BloodBulletProviderImplant(Entity entity)
    {

        this.entity = entity;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {

    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void tick(Runnable remove)
    {

    }
}
