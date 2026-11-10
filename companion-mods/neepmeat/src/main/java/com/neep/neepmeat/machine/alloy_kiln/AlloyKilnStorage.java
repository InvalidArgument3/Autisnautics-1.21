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

package com.neep.neepmeat.machine.alloy_kiln;

import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.meatlib.util.NbtSerialisable;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import com.neep.meatlib.transfer.InventoryStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class AlloyKilnStorage implements NbtSerialisable
{
    public static int FUEL = 0;
    public static int INPUT_1 = 1;
    public static int INPUT_2 = 2;
    public static int OUTPUT = 3;

    protected final ImplementedInventory inventory;
    private final InventoryStorage inventoryStorage;
    private final AlloyKilnBlockEntity parent;
    private final Storage<ItemVariant> inputStorage;

    public AlloyKilnStorage(AlloyKilnBlockEntity parent)
    {
        this.inventory = new ImplementedInventory()
        {
            final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);

            public NonNullList<ItemStack> getItems()
            {
                return items;
            }

            @Override
            public void setChanged()
            {
                ImplementedInventory.super.setChanged();
                parent.setChanged();
                parent.updateRecipe();
            }
        };
        this.inventoryStorage = InventoryStorage.of(inventory, null);
        this.inputStorage = new CombinedStorage<>(List.of(inventoryStorage.getSlot(INPUT_1), inventoryStorage.getSlot(INPUT_2)));
        this.parent = parent;
    }

    public Storage<ItemVariant> getStorage(Direction direction)
    {
        var facing = parent.getBlockState().getValue(AlloyKilnBlock.FACING);
        if (direction == null || direction == Direction.UP) {
            // Ingredient inputs
            return getInputStorage();
        }
        else if (direction == facing.getClockWise())
        {
            return inventoryStorage.getSlot(INPUT_1);
        }
        else if (direction == facing.getCounterClockWise())
        {
            return inventoryStorage.getSlot(INPUT_2);
        }
        else if (!direction.getAxis().isVertical())
        {
            return inventoryStorage.getSlot(FUEL);
        }
        else if (direction == Direction.DOWN)
        {
            return inventoryStorage.getSlot(OUTPUT);
        }
        return null;
    }

    public int decrementFuel()
    {
        if (!inventory.isEmpty())
        {
            ItemStack stack = inventory.getItem(FUEL);
            Item remainder = stack.getItem().getCraftingRemainingItem();
            Integer time = FuelRegistry.INSTANCE.get(stack.getItem());

            if (time != null)
            {
                stack.shrink(1);
                if (remainder != null && stack.isEmpty())
                {
                    inventory.setItem(FUEL, new ItemStack(remainder, 1));
                }
                inventory.setChanged();
                return time;
            }
        }
        return -1;
    }

    public SingleSlotStorage<ItemVariant> getSlot(int i)
    {
        return inventoryStorage.getSlot(i);
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        ContainerHelper.saveAllItems(nbt, inventory.getItems());
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        ContainerHelper.loadAllItems(nbt, inventory.getItems());
    }

    public void dropItems(Level world, BlockPos pos)
    {
        Containers.dropContents(world, pos, inventory);
    }

    public Storage<ItemVariant> getOutputStorage()
    {
        return inventoryStorage.getSlot(OUTPUT);
    }

    public Storage<ItemVariant> getInputStorage()
    {
        return inputStorage;
    }

    public void markDirty()
    {
        parent.setChanged();
        inventory.setChanged();
    }
}