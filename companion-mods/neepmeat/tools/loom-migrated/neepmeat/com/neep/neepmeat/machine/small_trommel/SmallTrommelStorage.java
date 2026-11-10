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

package com.neep.neepmeat.machine.small_trommel;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import java.util.Objects;

public class SmallTrommelStorage implements TrommelStorage, NbtSerialisable
{
    protected final LazyBlockApiCache<Storage<FluidVariant>, Direction> fluidInput;
    protected final WritableSingleFluidStorage fluidOutput;
    protected final WritableStackStorage itemOutput;

    protected SmallTrommelBlockEntity parent;

    public SmallTrommelStorage(SmallTrommelBlockEntity parent)
    {
        this.parent = parent;
//        this.fluidInput = new WritableSingleFluidStorage(FluidConstants.BUCKET, parent::markDirty)
//        {
//            @Override
//            protected boolean canInsert(FluidVariant variant)
//            {
//                return true;
////                return OreFatRegistry.getFromVariant(variant) != null;
//            }
//        };

        this.fluidInput = LazyBlockApiCache.of(FluidStorage.SIDED, parent.getBlockPos().above(), parent::getLevel, () -> Direction.DOWN);

        this.fluidOutput = new WritableSingleFluidStorage(FluidConstants.BUCKET, parent::setChanged);
        this.itemOutput = new WritableStackStorage(parent::setChanged, 16);
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
//        NbtCompound inputNbt = new NbtCompound();
//        fluidInput.toNbt(inputNbt);
//        nbt.put("fluidInput", inputNbt);

        CompoundTag outputNbt = new CompoundTag();
        fluidOutput.toNbt(outputNbt);
        nbt.put("fluidOutput", outputNbt);

        CompoundTag itemOutputNbt = new CompoundTag();
        itemOutput.writeNbt(itemOutputNbt);
        nbt.put("itemOutput", itemOutputNbt);

        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
//        this.fluidInput.readNbt((NbtCompound) Objects.requireNonNull(nbt.get("fluidInput")));
        this.fluidOutput.readNbt((CompoundTag) Objects.requireNonNull(nbt.get("fluidOutput")));
        this.itemOutput.readNbt((CompoundTag) Objects.requireNonNull(nbt.get("itemOutput")));
    }

    public SingleVariantStorage<FluidVariant> input()
    {
        var storage = fluidInput.find();
        if (storage instanceof SingleVariantStorage<FluidVariant> singleVariantStorage)
        {
            return singleVariantStorage;
        }

        return SINGLE_EMPTY;
    }

    public WritableSingleFluidStorage output()
    {
        return fluidOutput;
    }

    public WritableStackStorage itemOutput()
    {
        return itemOutput;
    }

    public static final SingleVariantStorage<FluidVariant> SINGLE_EMPTY = new SingleVariantStorage<>()
    {
        @Override
        protected FluidVariant getBlankVariant()
        {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(FluidVariant variant)
        {
            return 0;
        }
    };
}