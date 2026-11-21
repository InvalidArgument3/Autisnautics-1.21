/*
 * Copyright (c) MeatWheeze 2026.
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
import com.neep.meatlib.client.screen.primitive.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class Label extends AbstractClickableWidget implements Rectangle.Mutable
{
    public final Font textRenderer = Minecraft.getInstance().font;

    private final Supplier<Component> text;
    private int col;
    private boolean shadow = true;
    private boolean centre;

    public Label(Component text)
    {
        this(() -> text);
    }

    public Label(Supplier<Component> text)
    {
        this.text = text;
        this.col = 0xc1b2a5;
    }

    public Label col(int col)
    {
        this.col = col;
        return this;
    }

    public Label shadow(boolean shadow)
    {
        this.shadow = shadow;
        return this;
    }

    public Label centre(boolean centre)
    {
        this.centre = centre;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        if (centre)
            GUIUtil.drawCenteredText(context, textRenderer, text.get(), x0() + (float) nw() / 2, y0(), col, shadow);
        else
            context.drawString(textRenderer, text.get(), x0(), y0(), col, shadow);

    }

    @Override
    public void setDims(int x, int y, int w, int h)
    {
        setPos(x, y);
        this.width = w;
        this.height = h;
    }

    //    @Override
//    public void renderTooltip(DrawContext context, int mouseX, int mouseY, float delta)
//    {
//        // TODO: help tooltips
//        super.renderTooltip(context, mouseX, mouseY, delta);
//    }
}
