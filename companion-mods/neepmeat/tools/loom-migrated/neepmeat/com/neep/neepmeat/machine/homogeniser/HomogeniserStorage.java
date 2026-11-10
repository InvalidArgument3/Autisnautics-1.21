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

package com.neep.neepmeat.machine.homogeniser;

import com.neep.meatlib.util.LazySupplier;
import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class HomogeniserStorage implements NbtSerialisable
{
    protected final HomogeniserBlockEntity parent;
    protected final WritableStackStorage itemStorage;

    protected final LazySupplier<BlockApiCache<Storage<FluidVariant>, Direction>> fluidInput;
    protected final LazySupplier<BlockApiCache<Storage<FluidVariant>, Direction>> fluidOutput;

    protected final Direction facing;

    public HomogeniserStorage(HomogeniserBlockEntity parent)
    {
        this.parent = parent;
        this.itemStorage = new WritableStackStorage(parent::sync, 64);
        this.facing = parent.getBlockState().getValue(HomogeniserBlock.FACING);

        fluidInput = LazySupplier.of(() -> BlockApiCache.create(FluidStorage.SIDED, (ServerLevel) parent.getLevel(),
                        parent.getBlockPos().relative(facing.getOpposite())));

        fluidOutput = LazySupplier.of(() -> BlockApiCache.create(FluidStorage.SIDED, (ServerLevel) parent.getLevel(),
                parent.getBlockPos().relative(facing)));
    }

    @Nullable
    public Storage<FluidVariant> getInputFluidStorage()
    {
        return fluidInput.get().find(facing);
    }

    @Nullable
    Storage<FluidVariant> getOutputStorage()
    {
        return fluidOutput.get().find(facing.getOpposite());
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        itemStorage.writeNbt(nbt);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        itemStorage.readNbt(nbt);
    }
}
