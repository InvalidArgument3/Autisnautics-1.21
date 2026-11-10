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

package com.neep.neepmeat.machine.item_mincer;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.ItemVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.food.FoodProperties;

public class ItemMincerStorage implements NbtSerialisable
{
    protected final WritableSingleFluidStorage outputStorage;
    protected final WritableStackStorage inputStorage;

    public ItemMincerStorage(ItemMincerBlockEntity parent)
    {
        outputStorage = new WritableSingleFluidStorage(2 * FluidConstants.BUCKET, parent::sync)
        {
            @Override
            public boolean supportsInsertion()
            {
                return false;
            }
        };

        inputStorage = new WritableStackStorage(parent::sync, 64)
        {
            @Override
            protected boolean canInsert(ItemVariant variant)
            {
                FoodProperties food = variant.toStack().getFoodProperties(null);
                return food != null;
            }
        };
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        var inNbt = new CompoundTag();
        var outNbt = new CompoundTag();
        inputStorage.writeNbt(inNbt);
        outputStorage.toNbt(outNbt);
        nbt.put("output", outNbt);
        nbt.put("input", inNbt);

        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        outputStorage.readNbt(nbt.getCompound("output"));
        inputStorage.readNbt(nbt.getCompound("input"));
    }
}
