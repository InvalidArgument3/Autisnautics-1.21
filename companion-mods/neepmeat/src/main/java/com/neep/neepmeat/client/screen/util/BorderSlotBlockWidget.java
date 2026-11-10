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

package com.neep.neepmeat.client.screen.util;

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.slot.MobileSlot;
import com.neep.meatlib.client.screen.slot.SimpleSlotBlockWidget;
import com.neep.meatlib.theme.PLCCols;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;

public class BorderSlotBlockWidget extends SimpleSlotBlockWidget
{
    public BorderSlotBlockWidget(int x, int y, int rows, int cols, List<Slot> slots, BetterHandledScreen<?> handler)
    {
        super(x, y, rows, cols, slots, handler);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
    }

    protected void drawSlot(GuiGraphics context, MobileSlot mobileSlot)
    {
//        GUIUtil.drawTexture(GUIUtil.SCREEN_BORDER, context, mobileSlot.x() - 2, mobileSlot.y() - 2, 0, 236, 20, 20);
        GUIUtil.renderBorderInner(context, mobileSlot.x0() - 1, mobileSlot.y0() - 1, 18, 18, PLCCols.BORDER.col, 0);

        super.drawSlot(context, mobileSlot);
    }

    @Override
    protected void setSlotPositions()
    {
        setSlotPositions(x + 2, y + 2, slots, cols, rows, 17);
    }

    @Override
    public int nw()
    {
        return super.nw() + 4;
    }

    @Override
    public int nh()
    {
        return super.nh() + 4;
    }
}
