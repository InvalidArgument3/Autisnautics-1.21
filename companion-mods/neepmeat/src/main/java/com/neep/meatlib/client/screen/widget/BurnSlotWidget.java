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

package com.neep.meatlib.client.screen.widget;

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.slot.MobileSlot;
import com.neep.meatlib.client.screen.slot.SimpleSlotBlockWidget;
import com.neep.meatlib.client.screen.GUIUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.function.IntSupplier;

public class BurnSlotWidget extends SimpleSlotBlockWidget implements Rectangle, ClickableWidget
{
    private final IntSupplier burnTime;
    private final IntSupplier fuelTime;

    private final int flameHeight = 14;

    public BurnSlotWidget(int x, int y, int rows, int cols, List<Slot> slots, BetterHandledScreen<?> handler, IntSupplier burnTime, IntSupplier fuelTime)
    {
        super(x, y, rows, cols, slots, handler);
        this.burnTime = burnTime;
        this.fuelTime = fuelTime;
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        super.render(matrices, mouseX, mouseY, delta);

        drawBurnTime(matrices, x + (this.nw() - flameHeight) / 2, y + 2);
//            GUIUtil.renderBorderInner(matrices, x(), y(), w(), h(), PLCCols.TEXT.col, 0);
    }

    @Override
    protected void setSlotPositions()
    {
        setSlotPositions(x + 2, y + flameHeight + 1 + 2, slots, cols, rows);
    }

    @Override
    protected void renderSlot(GuiGraphics context, MobileSlot mobileSlot)
    {
        GUIUtil.drawTexture(GUIUtil.SCREEN_BORDER, context, mobileSlot.x0() - 2, mobileSlot.y0() - 2, 0, 236, 20, 20);
        super.renderSlot(context, mobileSlot);
    }

    @Override
    public int nw()
    {
        return super.nw() + 4;
    }

    @Override
    public int nh()
    {
        return super.nh() + flameHeight + 1 + 4;
    }

    public void drawBurnTime(GuiGraphics matrices, int i, int j)
    {
        GUIUtil.drawTexture(GUIUtil.INVENTORY_BACKGROUND, matrices, i - 1, j - 1, 176, 14, 14, 14);

        int time = burnTime.getAsInt();
        if (time < 1)
            return;

        int total = fuelTime.getAsInt();
        int k = (int) ((time / (total + 1f)) * 14);
        GUIUtil.drawTexture(GUIUtil.INVENTORY_BACKGROUND, matrices, i - 1, j + 12 - k, 176, 12 - k, 14, k + 1);
    }
}
