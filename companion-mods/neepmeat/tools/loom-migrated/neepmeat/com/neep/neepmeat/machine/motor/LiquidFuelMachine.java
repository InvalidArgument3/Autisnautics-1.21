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

package com.neep.neepmeat.machine.motor;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.processing.FluidEnegyRegistry;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.init.NMFluids;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("UnstableApiUsage")
public class LiquidFuelMachine extends SyncableBlockEntity
{
    protected WritableSingleFluidStorage fluidStorage = new WritableSingleFluidStorage(bufferCapacity(), this::setChanged);

    public LiquidFuelMachine(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public Storage<FluidVariant> getTank(Direction ignored)
    {
        return fluidStorage;
    }

    protected long maxPower()
    {
        return 1000;
    }

    protected long bufferCapacity()
    {
        double baseEnergy = FluidEnegyRegistry.getInstance().getOrEmpty(NMFluids.ETHEREAL_FUEL.still()).baseEnergy();
        return (long) (maxPower() / baseEnergy);
    }

    public long extractEnergy(long abs, TransactionContext transaction)
    {
        FluidVariant variant = fluidStorage.getResource();
        if (!variant.isBlank())
        {
            double baseEnergy = FluidEnegyRegistry.getInstance().getOrEmpty(variant.getFluid()).baseEnergy();
            long extractAmount = (long) (abs / baseEnergy);
            long extracted = fluidStorage.extract(variant, extractAmount, transaction);

            // This is highly wrong
            return (long) (extracted * baseEnergy);
        }
        return 0;
    }
}
