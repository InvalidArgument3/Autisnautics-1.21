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

package com.neep.neepmeat.machine.casting_basin;

import com.neep.meatlib.recipe.ImplementedRecipe;
import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

@SuppressWarnings("UnstableApiUsage")
public class CastingBasinStorage implements NbtSerialisable, ImplementedRecipe.DummyInventory
{
    protected WritableStackStorage outputStorage;
    protected WritableSingleFluidStorage inputStorage;
    protected CastingBasinBlockEntity parent;
    protected boolean locked;

    public CastingBasinStorage(CastingBasinBlockEntity parent)
    {
        this.parent = parent;
        this.outputStorage = new WritableStackStorage(this::syncOutput, 1)
        {
            @Override
            public boolean supportsInsertion()
            {
                return false;
            }
        };

        this.inputStorage = new WritableSingleFluidStorage(FluidConstants.INGOT, parent::sync)
        {
            @Override
            protected boolean canInsert(FluidVariant variant)
            {
                return super.canInsert(variant) && !locked;
            }

            protected boolean canExtract(FluidVariant variant)
            {
                return super.canExtract(variant) && !locked;
            }
        };
    }

    private void syncOutput()
    {
        parent.sync();
        parent.tryUpdateComparators();
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        CompoundTag inputNbt = new CompoundTag();
        inputStorage.toNbt(inputNbt);
        nbt.put("input", inputNbt);

        CompoundTag outputNbt = new CompoundTag();
        outputStorage.writeNbt(outputNbt);
        nbt.put("output", outputNbt);

        nbt.putBoolean("locked", locked);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        inputStorage.readNbt((CompoundTag) nbt.get("input"));
        outputStorage.readNbt((CompoundTag) nbt.get("output"));
        locked = nbt.getBoolean("locked");
    }

    public boolean lock()
    {
        if (!locked)
        {
            locked = true;
            return true;
        }
        return false;
    }

    public boolean unlock()
    {
        if (locked)
        {
            locked = false;
            return true;
        }
        return false;
    }

    public WritableSingleFluidStorage fluid(Direction dir)
    {
        return inputStorage;
    }

    public WritableStackStorage item(Direction dir)
    {
        return dir != Direction.UP ? outputStorage : null;
    }
}
