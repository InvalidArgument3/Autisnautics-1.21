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

import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.theme.PLCCols;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;

import static com.neep.meatlib.client.screen.GUIUtil.drawHorizontalLine1;
import static com.neep.meatlib.client.screen.GUIUtil.drawVerticalLine1;

public class BorderScrollRight extends Border
{
    public BorderScrollRight(int padding, Supplier<Integer> col)
    {
        this(0, 0, 1, 1, padding, col);
    }

    public BorderScrollRight(int x, int y, int w, int h, int padding, Supplier<Integer> col)
    {
        super(x, y, w, h, padding, col);
    }

    public BorderScrollRight(Rectangle bounds, int padding, Supplier<Integer> col)
    {
        super(bounds, padding, col);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        renderBorder(context, x, y, w - 1, h - 1, col.get(), 0);
        renderBorder(context, x, y, w - 1, h - 1, PLCCols.TRANSPARENT.col, -1);

        drawVerticalLine1(context, x + w - 1, y + 1, y + h, PLCCols.TRANSPARENT.col);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta, float scrollAmount)
    {
        if (Float.isNaN(scrollAmount))
            scrollAmount = 0;

        render(context, mouseX, mouseY, delta);

        float barHeight = 10;
        float barTravel = h - 4 - barHeight - 1;

        int yOff = (int) (y + 2 + scrollAmount * barTravel);
        drawVerticalLine1(context, x + w - 1, yOff, (int) (yOff + barHeight), col.get());
    }

    protected void renderBorder(GuiGraphics context, int x, int y, int dx, int dy, int col, int offset)
    {
        drawHorizontalLine1(context, x - offset, x + dx + offset, y - offset, col);
        drawVerticalLine1(context, x - offset, y - offset, y + dy + offset, col);
        drawHorizontalLine1(context, x - offset, x + dx + offset, y + dy + offset, col);

        drawVerticalLine1(context, x + dx + offset, y - offset, y + 2, col);
        drawVerticalLine1(context, x + dx + offset, y - offset + dy - 2, y + offset + dy, col);
    }
}
