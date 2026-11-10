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

package com.neep.neepmeat.item;

import com.neep.neepmeat.api.processing.MeatFluidUtil;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.StoragePreconditions;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

@SuppressWarnings("UnstableApiUsage")
public class MeatCartonStorage implements InsertionOnlyStorage<FluidVariant>
{
    private final ContainerItemContext context;
    private final Item emptyItem;
    private final Item fullitem;
    private final Fluid insertableFluid;
    private final long insertableAmount;

    public MeatCartonStorage(ContainerItemContext context, Item emptyItem, Item fullitem, Fluid insertableFluid, long insertableAmount)
    {
        this.context = context;
        this.emptyItem = emptyItem;
        this.fullitem = fullitem;
        this.insertableFluid = insertableFluid;
        this.insertableAmount = insertableAmount;
    }

    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction)
    {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (!context.getItemVariant().isOf(emptyItem)) return 0;

        if (resource.isOf(insertableFluid) && maxAmount >= insertableAmount)
        {
            CompoundTag nbt = MeatFluidUtil.copyRootRounded(new CompoundTag(), resource.getNbt());
            ItemVariant newVariant = ItemVariant.of(fullitem, nbt);

            long exc = context.exchange(newVariant, 1, transaction);
            if (exc == 1)
            {
                return insertableAmount;
            }
        }

        return 0;
    }
}
