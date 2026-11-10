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

package com.neep.neepmeat.api.storage;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class MultiTypedFluidBuffer extends CombinedStorage<FluidVariant, TypedFluidBuffer>
{
    protected BlockEntity parent;
    List<TypedFluidBuffer> buffers;

    public MultiTypedFluidBuffer(BlockEntity parent, List<TypedFluidBuffer> buffers)
    {
        super(buffers);
        this.buffers = buffers;
        this.parent = parent;
    }

    public CompoundTag writeNbt(CompoundTag nbt)
    {
        for (int i = 0; i < buffers.size(); ++i)
        {
            CompoundTag nbt2 = new CompoundTag();
            buffers.get(i).toNbt(nbt2);
            nbt.put("buffer_" + i, nbt2);
        }
        return nbt;
    }

    public void readNbt(CompoundTag nbt)
    {
        for (int i = 0; i < buffers.size(); ++i)
        {
            CompoundTag nbt2 = nbt.getCompound("buffer_" + i);
            buffers.get(i).readNbt(nbt2);
        }
    }
}
