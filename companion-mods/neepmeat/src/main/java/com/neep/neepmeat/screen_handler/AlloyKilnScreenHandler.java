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

package com.neep.neepmeat.screen_handler;

import com.neep.neepmeat.init.NMScreenHandlers;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceFuelSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AlloyKilnScreenHandler extends BasicScreenHandler
{
    // Client
    public AlloyKilnScreenHandler(int syncId, Inventory playerInventory)
    {
        this(syncId, playerInventory, new SimpleContainer(4), new SimpleContainerData(4));
    }

    // Server
    public AlloyKilnScreenHandler(int syncId, Inventory playerInventory, Container inventory, ContainerData delegate)
    {
        super(NMScreenHandlers.ALLOY_KILN, playerInventory, inventory, syncId, delegate);
        checkContainerSize(inventory, 4);

        createSlots();
        createPlayerSlots(8, 95, playerInventory);
    }

    @Override
    public boolean stillValid(Player player)
    {
        return this.inventory.stillValid(player);
    }

    protected void createSlots()
    {
        this.addSlot(new FuelSlot(inventory, 0, 56, 53)); // Fuel
        this.addSlot(new Slot(inventory, 1, 45, 17)); // Left ingredient
        this.addSlot(new Slot(inventory, 2, 67, 17)); // Right Ingredient
        this.addSlot(new Slot(inventory, 3, 116, 35) // Output
        {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return false;
            }
        });
    }

    public static class FuelSlot extends Slot
    {
        public FuelSlot(Container inventory, int index, int x, int y)
        {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack)
        {
            return FuelRegistry.INSTANCE.get(stack.getItem()) != null || FurnaceFuelSlot.isBucket(stack);
        }

        @Override
        public int getMaxStackSize(ItemStack stack)
        {
            return FurnaceFuelSlot.isBucket(stack) ? 1 : super.getMaxStackSize(stack);
        }
    }
}