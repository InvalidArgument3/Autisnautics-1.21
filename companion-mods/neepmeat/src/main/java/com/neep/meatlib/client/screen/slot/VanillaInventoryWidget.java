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

package com.neep.meatlib.client.screen.slot;

import com.neep.meatlib.client.screen.AbstractSlotBlockWidget;
import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.GUIUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class VanillaInventoryWidget extends AbstractSlotBlockWidget implements Rectangle, ClickableWidget
{
    private boolean focused;
    private final List<MobileSlot> inventory;
    private final List<MobileSlot> hotbar;

    public VanillaInventoryWidget(int x, int y, List<Slot> slots, BetterHandledScreen<?> handler)
    {
        super(x, y, slots, handler);
        this.inventory = this.slots.subList(0, 3 * 9);
        this.hotbar = this.slots.subList(3 * 9, 3 * 9 + 9);
    }

    public VanillaInventoryWidget(List<Slot> slots, BetterHandledScreen<?> handler)
    {
        this(0, 0, slots, handler);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        setSlotPositions(x + 8, y + 8, inventory, 9, 3);
        setSlotPositions(x + 8, y + 8 + 58, hotbar, 9, 1);

        GUIUtil.drawInventoryBackground(context, x, y);
        super.render(context, mouseX, mouseY, delta);
        // putting this before interferes with tooltips
        // why???
    }

    @Override
    public int nw()
    {
        return 176;
    }

    @Override
    public int nh()
    {
        return 90;
    }

    @Override
    public void setFocused(boolean focused)
    {
        this.focused = focused;
    }

    @Override
    public boolean isFocused()
    {
        return focused;
    }
}
