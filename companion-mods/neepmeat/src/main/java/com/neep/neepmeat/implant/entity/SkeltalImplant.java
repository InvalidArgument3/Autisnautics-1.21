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

package com.neep.neepmeat.implant.entity;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

// Thank Mr Skeltal for calcium and good bones
public class SkeltalImplant implements EntityImplant
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "skeltal");

    protected final Player player;

    public SkeltalImplant(Player player)
    {
        this.player = player;
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

    @Override
    public float getProtectionAmount(DamageSource source, float amount)
    {
//        if (source.isIn(DamageTypeTags.IS_FALL))
        if (source.is(DamageTypeTags.IS_FALL))
        {
            return 20f;
        }

        return 0;
    }
}
