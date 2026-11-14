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

import com.neep.meatlib.client.screen.primitive.Rectangle;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class IntNumberTextField extends AbstractNumberTextField<Integer> implements Rectangle.Mutable
{
    private final Consumer<IntNumberTextField> update;
    private final int min;
    private final int max;

    private final int minIncrement;
    private final int maxIncrement;

    public IntNumberTextField(Font textRenderer, int x, int y, int width, int height, Component prefix, Consumer<IntNumberTextField> update,
                              int min, int max)
    {
        this(textRenderer, x, y, width, height, prefix, update, min, max, 1, 16);
    }

    public IntNumberTextField(Font textRenderer, int x, int y, int width, int height, Component prefix, Consumer<IntNumberTextField> update,
                              int min, int max, int minIncrement, int maxIncrement)
    {
        super(textRenderer, x, y, width, height, prefix);
        this.update = update;
        this.min = min;
        this.max = max;
        this.minIncrement = minIncrement;
        this.maxIncrement = maxIncrement;
    }

    @Override
    protected void update()
    {
        update.accept(this);
    }

    @Override
    protected boolean isValidString(String text)
    {
        return text.matches("[+\\-0-9E]*");
    }

    @Override
    protected String valueToString(Integer value)
    {
        return Integer.toString(value);
    }

    protected int parseValue(String text)
    {
        try
        {
            return Mth.clamp(Integer.parseInt(text), min, max);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    private float scrollMultiplier()
    {
        return Screen.hasShiftDown() ? minIncrement : maxIncrement;
    }

    @Override
    protected Integer applyScroll(String text, double amount)
    {
        return Mth.clamp((int) (parseValue(text) + amount * scrollMultiplier()), min, max);
    }

    public int parse()
    {
        return parseValue(text());
    }

    @Override
    public void setDims(int x, int y, int w, int h)
    {
        super.setDims(x, y, w, h);
    }
}
