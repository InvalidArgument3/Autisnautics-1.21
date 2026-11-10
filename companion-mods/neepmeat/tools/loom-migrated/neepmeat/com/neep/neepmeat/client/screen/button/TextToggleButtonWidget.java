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

package com.neep.neepmeat.client.screen.button;

import com.neep.meatlib.client.screen.button.NMButtonWidget;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TextToggleButtonWidget extends NMButtonWidget implements Rectangle.Mutable
{
    private final BooleanSupplier toggled;
    private final ToggleAction onToggle;

    public TextToggleButtonWidget(int x, int y, int width, int height, BooleanSupplier toggled, Component message, ToggleAction onToggle)
    {
        super(x, y, width, height, message, b -> {});
        this.toggled = toggled;
        this.onToggle = onToggle;
    }

    @Override
    public void renderWidget(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        super.renderWidget(matrices, mouseX, mouseY, delta);
    }

    public boolean isToggled()
    {
        return toggled.getAsBoolean();
    }

    @Override
    public boolean borderActive()
    {
        return isToggled();
    }

    @Override
    protected boolean textActive()
    {
        return isHovered();
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        super.onClick(mouseX, mouseY);
        onToggle.toggle(this, !toggled.getAsBoolean());
    }

    @Override
    public void setSize(int w, int h)
    {
        width = w;
        height = h;
    }

    @Override
    public void setDims(int x, int y, int w, int h)
    {
        setPos(x, y);
        setSize(w, h);
    }

    @FunctionalInterface
    public interface ToggleAction
    {
        void toggle(TextToggleButtonWidget button, boolean toggled);
    }
}
