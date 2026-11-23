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

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class SimpleHandledScreen<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T>
{
    protected final T handler;

    protected SimpleHandledScreen(T handler, Component title)
    {
        super(title);
        this.handler = handler;
    }

    @Override
    public T getMenu()
    {
        return handler;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (super.keyPressed(keyCode, scanCode, modifiers))
        {
            return true;
        }
        if (minecraft.options.keyInventory.matches(keyCode, scanCode))
        {
            this.onClose();
            return true;
        }
        return true;
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public final void tick()
    {
        super.tick();
        if (!minecraft.player.isAlive() || minecraft.player.isRemoved())
        {
            minecraft.player.closeContainer();
        }
    }

    @Override
    public void removed()
    {
        if (minecraft.player == null)
        {
            return;
        }
        this.handler.removed(minecraft.player);
    }

    @Override
    public void onClose()
    {
        minecraft.player.closeContainer();
        onClose();
    }
}
