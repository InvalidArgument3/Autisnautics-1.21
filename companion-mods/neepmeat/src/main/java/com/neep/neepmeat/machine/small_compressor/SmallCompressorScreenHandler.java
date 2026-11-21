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

import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SmallCompressorScreenHandler extends BasicScreenHandler
{
    public SmallCompressorScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, new SimpleContainer(1), syncId, new SimpleContainerData(2));
    }

    public SmallCompressorScreenHandler(Inventory playerInventory, Container inventory, int syncId, ContainerData delegate)
    {
        super(NMScreenHandlers.SMALL_COMPRESSOR, playerInventory, inventory, syncId, delegate);
        checkContainerSize(inventory, 1);

        this.addSlot(new Slot(inventory, 0, 80, 52)
        {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                Integer entry = FuelRegistry.INSTANCE.get(stack.getItem());
                return entry != null && entry > 0;
            }
        });
        createPlayerSlots(8, 91 + 8, playerInventory);
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType actionType, Player player)
    {
        super.clicked(slotIndex, button, actionType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        return super.quickMoveStack(player, index);
    }
}
