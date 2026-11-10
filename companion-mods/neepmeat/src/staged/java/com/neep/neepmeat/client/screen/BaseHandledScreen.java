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

package com.neep.neepmeat.client.screen;

import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class BaseHandledScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T>
{
    public BaseHandledScreen(T handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
    }

    @Override
    public void removed()
    {
        // removed() no longer closes the screen handler.
        // This allows EMI screens to replace the current screen without closing the packet handlers.
        if (menu instanceof BasicScreenHandler handler1)
        {
            handler1.onRemoved();
        }

        super.removed();
    }
}
