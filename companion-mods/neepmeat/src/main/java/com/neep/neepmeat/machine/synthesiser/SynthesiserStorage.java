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

package com.neep.neepmeat.machine.synthesiser;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.item.EssentialSaltesItem;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.CombinedStorage;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class SynthesiserStorage implements NbtSerialisable
{
    protected final WritableSingleFluidStorage meatStorage;
    protected final WritableSingleFluidStorage fluidStorage;
    protected final CombinedStorage<FluidVariant, WritableSingleFluidStorage> combined;
    protected final WritableStackStorage itemStorage;

    public static Storage<FluidVariant> getFluidStorage(SynthesiserBlockEntity be, Direction direction)
    {
        if (direction == null)
            return be.storage.combined;

        return direction.getAxis() != Direction.Axis.Y ? be.storage.combined : null;
    }

    public SynthesiserStorage(SynthesiserBlockEntity parent)
    {
        this.meatStorage = new WritableSingleFluidStorage(FluidConstants.BUCKET * 9, parent::sync)
        {
            @Override
            protected boolean canInsert(FluidVariant variant)
            {
                return variant.isOf(com.neep.neepmeat.fluid.FluidForms.still(NMFluids.MEAT));
            }
        };

        this.fluidStorage = new WritableSingleFluidStorage(FluidConstants.BUCKET * 9, parent::sync)
        {
            @Override
            protected boolean canInsert(FluidVariant variant)
            {
                return super.canInsert(variant);
            }
        };

        combined = new CombinedStorage<>(List.of(meatStorage));
        itemStorage = new WritableStackStorage(parent::sync);
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.put("meatStorage", meatStorage.toNbt(new CompoundTag()));
        nbt.put("itemStorage", itemStorage.toNbt(new CompoundTag()));

        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        this.meatStorage.readNbt(nbt.getCompound("meatStorage"));
        this.itemStorage.readNbt(nbt.getCompound("itemStorage"));
    }

    @Nullable
    public EntityType<?> getEntityType()
    {
        ItemVariant stored = itemStorage.getResource();

        return EssentialSaltesItem.getEntityType(stored);
    }
}
