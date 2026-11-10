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

package com.neep.meatlib.client.screen.auto;

import com.neep.meatlib.theme.PLCCols;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import static com.neep.meatlib.client.screen.GUIUtil.drawVerticalLine1;

public class ScrollVStackPanel extends VStackPanel
{
    private int scrollAmount;

    public ScrollVStackPanel(int padding, int elementPadding, boolean centre, boolean fitElements)
    {
        super(padding, elementPadding, centre, fitElements);
        sideMargin = 1;
    }

    @Override
    protected int yOffset()
    {
        return scrollAmount;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        int offScreen = Math.max(0, elementsHeight - h);

        int prev = scrollAmount;
        scrollAmount = Mth.clamp((int) (scrollAmount + amount * 5), -offScreen, 0);

        if (scrollAmount != prev)
            init();

        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        context.enableScissor(x, y, x + w, y + h);
        super.render(context, mouseX, mouseY, delta);
        context.disableScissor();

        int offScreen = Math.max(0, elementsHeight - h);
        float scrollFrac = Mth.clamp(offScreen > 0 ? -scrollAmount / (float) offScreen : 0, 0.0f, 1.0f);

        if (Float.isNaN(scrollFrac))
            scrollFrac = 0;

        float barHeight = 10;
        float barTravel = h - 4 - barHeight - 1;

        int yOff = (int) (y + 2 + scrollFrac * barTravel);
        drawVerticalLine1(context, x + w - 1, y + 1, y + h, PLCCols.TRANSPARENT.col);
        drawVerticalLine1(context, x + w - 1, yOff, (int) (yOff + barHeight), PLCCols.BORDER.col);
    }
}
