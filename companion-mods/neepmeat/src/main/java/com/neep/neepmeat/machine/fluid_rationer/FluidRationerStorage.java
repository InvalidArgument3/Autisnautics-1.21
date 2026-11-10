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

package com.neep.neepmeat.machine.fluid_rationer;

import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.meatlib.transfer.ContainerItemContext;
import com.neep.meatlib.transfer.FluidStorage;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import java.util.HashSet;
import java.util.Set;

public class FluidRationerStorage extends WritableSingleFluidStorage
{
    SimpleContainer inventory;
    protected Set<FluidVariant> filters = new HashSet<>();

    public FluidRationerStorage(long capacity, Runnable finalCallback)
    {
        super(capacity, finalCallback);
        this.inventory = new SimpleContainer(1);
        inventory.addListener(sender ->
        {
            updateFilter(inventory.getItem(0));
            finalCallback.run();
        });
    }

    public void updateFilter(ItemStack stack)
    {
        filters.clear();
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        if (storage != null)
        {
            for (StorageView<FluidVariant> view : storage)
            {
                filters.add(view.getResource());
            }
        }
    }

    public boolean matchesFilter(FluidVariant variant)
    {
        return filters.isEmpty() || filters.contains(variant);
    }

    @Override
    public CompoundTag toNbt(CompoundTag nbt)
    {
        super.toNbt(nbt);
        nbt.put("inventory", this.inventory.createTag());
        return nbt;
    }

    @Override
    public CompoundTag readNbt(CompoundTag nbt)
    {
        super.readNbt(nbt);
        this.inventory.fromTag(nbt.getList("inventory", Tag.TAG_COMPOUND));
        return nbt;
    }
}
