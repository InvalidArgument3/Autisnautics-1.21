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

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.theme.PLCCols;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.world.inventory.Slot;

public class DraggableInventoryWidget extends VanillaInventoryWidget implements ClickableWidget, NarratableEntry
{
    private boolean focused;
    private boolean dragging;
    private int dragX;
    private int dragY;

    public DraggableInventoryWidget(int x, int y, List<Slot> slots, BetterHandledScreen<?> handler)
    {
        super(x, y, slots, handler);
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

    @Override
    public int nw()
    {
        return PLCCols.INV_W;
    }

    @Override
    public int nh()
    {
        return PLCCols.INV_H;
    }

    @Override
    public NarrationPriority narrationPriority()
    {
        return focused ? NarrationPriority.FOCUSED : NarrationPriority.NONE;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        if (dragging)
        {
            setPos(mouseX + dragX, mouseY + dragY);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (isWithin(mouseX, mouseY) && getSlotAt(mouseX, mouseY) == null)
        {
            dragging = true;
            dragX = (int) (x - mouseX);
            dragY = (int) (y - mouseY);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return isWithin(mouseX, mouseY);
    }

    @Override
    public void updateNarration(NarrationElementOutput builder)
    {

    }
}
