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
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.client.screen.widget.PlayerSlotsBorder;
import com.neep.meatlib.screen.MeatlibScreenHandlers;
import com.neep.meatlib.theme.PLCCols;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;

public class OutlineInventoryWidget extends AbstractSlotBlockWidget implements Rectangle, ClickableWidget
{
    private final boolean background;
    private boolean focused;
    private final List<MobileSlot> inventory;
    private final List<MobileSlot> hotbar;

    public OutlineInventoryWidget(List<Slot> slots, BetterHandledScreen<?> handler, boolean background)
    {
        super(0, 0, slots, handler);
        this.background = background;
        this.inventory = this.slots.subList(0, 3 * 9);
        this.hotbar = this.slots.subList(3 * 9, 3 * 9 + 9);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
//        if (background)
            context.fill(x, y, x + nw(), y + nh(), 0x90000000);

        PlayerSlotsBorder.renderSlotBorders(context, x, y, PLCCols.BORDER.col, PLCCols.TRANSPARENT.col);

        setSlotPositions(x + 2, y + 2, inventory, 9, 3);
        setSlotPositions(x + 2, y + 2 + 58, hotbar, 9, 1);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public int nw()
    {
        return MeatlibScreenHandlers.playerSlotsW();
    }

    @Override
    public int nh()
    {
        return MeatlibScreenHandlers.playerInvH() + 1;
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
