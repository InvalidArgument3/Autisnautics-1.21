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
import com.neep.meatlib.theme.PLCCols;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;

public class CheckboxWidget extends AbstractClickableWidget implements TooltipRenderer
{
    protected final Font textRenderer = client.font;
    private final BooleanSupplier toggled;
    private final ToggleAction onToggle;

    public CheckboxWidget(int x, int y, int w, int h, BooleanSupplier toggled, Component message, ToggleAction onToggle)
    {
        super(x, y, w, h, message);
        this.toggled = toggled;
        this.onToggle = onToggle;
    }

    public CheckboxWidget(int w, int h, BooleanSupplier toggled, Component message, ToggleAction onToggle)
    {
        super(0, 0, w, h, message);
        this.toggled = toggled;
        this.onToggle = onToggle;
    }

    public boolean borderActive(int mouseX, int mouseY)
    {
        return isMouseOver(mouseX, mouseY);
    }

    public boolean isStateTriggered()
    {
        return toggled.getAsBoolean();
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        String box = isStateTriggered() ? "☒" : "☐";
        int borderCol = borderActive(mouseX, mouseY) ? PLCCols.SELECTED.col : PLCCols.BORDER.col;

        int boxWidth = textRenderer.width(box);
        int boxHeight = textRenderer.lineHeight;

        int textX = x0() + (nh() - boxWidth) / 2;
        int textY = this.y0() + (nh() - boxHeight) / 2;

        GUIUtil.drawText(context, textRenderer, Component.literal(box), textX, textY, borderCol, false);
        GUIUtil.drawText(context, textRenderer, getMessage(), textX + boxWidth + 3, textY, PLCCols.TEXT.col, false);
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        super.onClick(mouseX, mouseY);
        boolean prevToggled = isStateTriggered();
        onToggle.toggle(this, !prevToggled);
    }

    @FunctionalInterface
    public interface ToggleAction
    {
        void toggle(CheckboxWidget button, boolean toggled);
    }
}
