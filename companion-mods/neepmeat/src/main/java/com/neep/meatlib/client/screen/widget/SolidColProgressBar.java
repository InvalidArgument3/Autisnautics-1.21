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

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.auto.SplitPanel;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.util.FloatSupplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class SolidColProgressBar extends AbstractClickableWidget implements Rectangle.Mutable
{
    private final SplitPanel.Split split;
    private final Font textRenderer;
    private final int col;

    private final FloatSupplier fillRatio;
    private final Supplier<Component> textSupplier;

    public SolidColProgressBar(SplitPanel.Split split, Font textRenderer, int col, FloatSupplier fillRatio, Supplier<Component> textSupplier)
    {
        this.split = split;
        this.textRenderer = textRenderer;
        this.col = col;
        this.fillRatio = fillRatio;
        this.textSupplier = textSupplier;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        GUIUtil.renderBorderInner(context, x0(), this.y0(), nw(), nh(), col, 0);

        if (split == SplitPanel.Split.HORIZONTAL)
        {
            int fillW = Math.round((nw() - 4) * Math.min(1, fillRatio.getAsFloat()));
            GUIUtil.fill(context, x0() + 2, this.y0() + 2, x0() + 2 + fillW, this.y0() + nh() - 2, col);
            renderSignText(context, mouseX, mouseY, delta);
        }
        else
        {
            int fillH = Math.round((nh() - 4) * Math.min(1, fillRatio.getAsFloat()));
            GUIUtil.fill(context, x0() + 2, this.y0() + 2, x0() + nw() - 2, this.y0() + 2 + fillH, col);
        }
    }

    protected void renderSignText(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        GUIUtil.drawCenteredText(context, textRenderer, textSupplier.get(), x0() + nw() / 2f, this.y0() + Math.round((nh() - textRenderer.lineHeight) / 2f), col, true);
    }

    @Override
    public void setDims(int x, int y, int w, int h)
    {
        setPos(x, y);
        this.width = w;
        this.height = h;
    }
}
