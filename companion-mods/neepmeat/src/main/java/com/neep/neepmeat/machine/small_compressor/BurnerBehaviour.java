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

package com.neep.neepmeat.machine.small_compressor;

import com.neep.meatlib.util.NbtSerialisable;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerData;

public class BurnerBehaviour implements NbtSerialisable
{
    private final Container inventory;
    private final BooleanConsumer updateBurn;
    private int burnTime;
    private int maxBurnTime;

    private boolean updateRequired = true;

    private final Delegate delegate = new Delegate();

    public BurnerBehaviour(Container inventory, BooleanConsumer updateBurn)
    {
        this.inventory = inventory;
        this.updateBurn = updateBurn;
    }

                            @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putInt("max_burn_time", maxBurnTime);
        nbt.putInt("burn_time", burnTime);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        this.maxBurnTime = nbt.getInt("max_burn_time");
        this.burnTime = nbt.getInt("burn_time");
    }

    public int getBurnTime()
    {
        return burnTime;
    }

    public int getMaxBurnTime()
    {
        return maxBurnTime;
    }

    public void tick()
    {
        burnTime = Math.max(0, burnTime - 1);

        if (burnTime == 0)
        {
            Integer time = FuelRegistry.INSTANCE.get(inventory.getItem(0).getItem());
            if (time != null)
            {
                maxBurnTime = time;
                burnTime = maxBurnTime;
                inventory.getItem(0).shrink(1);
            }

            updateRequired = true;
        }

        if (updateRequired)
        {
            updateBurn.accept(burnTime > 0);
            updateRequired = false;
        }
    }

    public ContainerData getDelegate()
    {
        return delegate;
    }

    public boolean isLit()
    {
        return getBurnTime() > 0;
    }

    private class Delegate implements ContainerData
    {
        @Override
        public int get(int index)
        {
            return switch (index)
            {
                case 0 -> burnTime;
                case 1 -> maxBurnTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value)
        {

        }

        @Override
        public int getCount()
        {
            return 2;
        }
    }
}
