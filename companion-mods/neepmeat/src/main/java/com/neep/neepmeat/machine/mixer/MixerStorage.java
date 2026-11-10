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

package com.neep.neepmeat.machine.mixer;

import com.neep.meatlib.recipe.ImplementedRecipe;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.CombinedStorage;
import com.neep.meatlib.transfer.ExtractionOnlyStorage;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class MixerStorage implements ImplementedRecipe.DummyInventory
{
    protected MixerBlockEntity parent;

    protected WritableSingleFluidStorage fluidInput1;
    protected WritableSingleFluidStorage fluidInput2;
    protected WritableSingleFluidStorage fluidOutput;
    protected Storage<FluidVariant> extractionOnlyOutput;
    protected WritableStackStorage itemInput;

    // Silly method of remembering the input fluids to show their particles
    public FluidVariant displayInput1 = FluidVariant.BLANK;
    public FluidVariant displayInput2 = FluidVariant.BLANK;

    public MixerStorage(MixerBlockEntity parent)
    {
        this.parent = parent;
        Runnable callback = parent::sync;
        fluidInput1 = new WritableSingleFluidStorage(FluidConstants.BUCKET, callback);
        fluidInput2 = new WritableSingleFluidStorage(FluidConstants.BUCKET, callback);
        itemInput = new WritableStackStorage(parent::sync);
        fluidOutput = new WritableSingleFluidStorage(2 * FluidConstants.BUCKET, callback);

        extractionOnlyOutput = new ExtractionOnlyStorage<>()
        {
            @Override
            public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction)
            {
                return fluidOutput.extract(resource, maxAmount, transaction);
            }

            @Override
            public Iterator<StorageView<FluidVariant>> iterator()
            {
                return fluidOutput.iterator();
            }
        };
    }

    public Storage<FluidVariant> getInputStorages()
    {
        List<Storage<FluidVariant>> storages = parent.getAdjacentStorages();
        return new CombinedStorage<>(storages);
    }

    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.put("displayInput1", displayInput1.toNbt());
        nbt.put("displayInput2", displayInput2.toNbt());
        
        CompoundTag input1 = new CompoundTag();
        fluidInput1.toNbt(input1);
        nbt.put("input_1", input1);

        CompoundTag input2 = new CompoundTag();
        fluidInput2.toNbt(input2);
        nbt.put("input_2", input2);

        CompoundTag itemNbt = new CompoundTag();
        itemInput.writeNbt(itemNbt);
        nbt.put("item_input", itemNbt);

        CompoundTag output = new CompoundTag();
        fluidOutput.toNbt(output);
        nbt.put("output", output);

        return nbt;
    }

    public void readNbt(CompoundTag nbt)
    {
        this.displayInput1 = FluidVariant.fromNbt(nbt.getCompound("displayInput1"));
        this.displayInput2 = FluidVariant.fromNbt(nbt.getCompound("displayInput2"));
        
        CompoundTag input1 = nbt.getCompound("input_1");
        fluidInput1.readNbt(input1);

        CompoundTag input2 = nbt.getCompound("input_2");
        fluidInput2.readNbt(input2);

        CompoundTag itemNbt = nbt.getCompound("item_input");
        itemInput.readNbt(itemNbt);

        CompoundTag output = nbt.getCompound("output");
        fluidOutput.readNbt(output);
    }

    public WritableStackStorage getItemInput()
    {
        return itemInput;
    }

    public Storage<FluidVariant> getFluidOutput()
    {
        return fluidOutput;
    }

    public Storage<FluidVariant> getExtractOutput()
    {
        return extractionOnlyOutput;
    }

    private static class FluidOutputStorage extends WritableSingleFluidStorage
    {
        public boolean locked = true;

        public FluidOutputStorage(long capacity, Runnable finalCallback)
        {
            super(capacity, finalCallback);
        }

        @Override
        protected boolean canInsert(FluidVariant variant)
        {
            return !locked && super.canInsert(variant);
        }
    }
}
