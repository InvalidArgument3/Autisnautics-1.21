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

package com.neep.neepmeat.transport.screen;

import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.item.filter.FilterList;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class FilterScreenHandler extends BasicScreenHandler
{
    public static final int BACKGROUND_WIDTH = 215;
    public static final int BACKGROUND_HEIGHT = 239;

    public final FilterWidgetHandler filterHandler;

    public FilterScreenHandler(int syncId, Inventory playerInventory)
    {
        this(new FilterList(1, () -> {}), playerInventory, syncId);
    }

    public FilterScreenHandler(FilterList filter, Inventory playerInventory, int syncId)
    {
        super(NMScreenHandlers.FILTER, playerInventory, new SimpleContainer(0), syncId, null);
        this.filterHandler = addWidgetHandler(new FilterWidgetHandler(playerInventory.player, filter));

        createInventory(8, BACKGROUND_HEIGHT - 80, playerInventory);
        createHotbar(8, BACKGROUND_HEIGHT - 22, playerInventory);
    }

    public static class Factory implements MenuProvider
    {
        private final FilterList filterList;

        public Factory(FilterList filterList)
        {
            this.filterList = filterList;
        }

        @Override
        public Component getDisplayName()
        {
            return Component.empty();
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player)
        {
            return new FilterScreenHandler(filterList, playerInventory, syncId);
        }
    }
}