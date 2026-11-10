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

package com.neep.neepmeat.machine.stirling_engine;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@SuppressWarnings("UnstableApiUsage")
public class StirlingEngineStorage
{
    protected SimpleContainer inventory;

    public StirlingEngineStorage(StirlingEngineBlockEntity parent)
    {
        this.inventory = new SimpleContainer(1);
    }

    public Storage<ItemVariant> getFuelStorage(Direction direction)
    {
        return InventoryStorage.of(inventory, direction);
    }

    public Container getInventory()
    {
        return inventory;
    }

    public int decrementFuel()
    {
        if (!inventory.isEmpty())
        {
            ItemStack stack = inventory.getItem(0);
            Item remainder = stack.getItem().getCraftingRemainingItem();
            Integer time = FuelRegistry.INSTANCE.get(stack.getItem());

            if (time != null)
            {
                stack.shrink(1);
                if (remainder != null && stack.isEmpty())
                {
                    inventory.addItem(new ItemStack(remainder, 1));
                }
                return time;
            }
        }

        return -1;
    }

    public void dropItems(Level world, BlockPos pos)
    {
        Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, inventory.getItem(0));
    }

    public void writeNbt(CompoundTag nbt)
    {
        nbt.put("inventory", this.inventory.createTag());
    }

    public void readNbt(CompoundTag nbt)
    {
        this.inventory.fromTag((ListTag) nbt.get("inventory"));
    }
}
