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

import com.neep.meatlib.client.screen.widget.config.AbstractConfigTextField;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public abstract class AbstractNumberTextField<T extends Number> extends AbstractConfigTextField
{
    public AbstractNumberTextField(Font textRenderer, int x, int y, int width, int height, Component prefix)
    {
        super(textRenderer, x, y, width, height, prefix);
    }

    protected abstract String valueToString(T value);

    protected abstract T applyScroll(String prevText, double amount);

    public void setNumberText(T value)
    {
        ignoreChange = true;
        String newText = valueToString(value);
        setText(newText);
        prevText = newText;
        ignoreChange = false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double amount)
    {
        setText(valueToString(applyScroll(text(), amount)));
        if (dirty)
            update();


        return super.mouseScrolled(mouseX, mouseY, scrollX, amount);
    }
}
