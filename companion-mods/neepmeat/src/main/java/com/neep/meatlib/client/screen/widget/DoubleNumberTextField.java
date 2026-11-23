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
import com.neep.meatlib.util.FormatUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class DoubleNumberTextField extends AbstractNumberTextField<Double> implements Rectangle.Mutable
{
    private final Runnable update;
    private final double min;
    private final double max;

    private final float minIncrement;
    private final float maxIncrement;

    public DoubleNumberTextField(Font textRenderer, int x, int y, int width, int height, Component prefix, Runnable update,
                                 double min, double max)
    {
        this(textRenderer, x, y, width, height, prefix, update, min, max, 1, 16);
    }

    public DoubleNumberTextField(Font textRenderer, int x, int y, int width, int height, Component prefix, Runnable update,
                                 double min, double max, int minIncrement, int maxIncrement)
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
        update.run();
    }

    @Override
    protected boolean isValidString(String text)
    {
        return text.matches("[+\\-0-9E.]*");
    }

    @Override
    protected String valueToString(Double value)
    {
        return FormatUtil.formatDouble(value);
    }

    protected double parseValue(String text)
    {
        try
        {
            return Mth.clamp(Double.parseDouble(text), min, max);
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
    protected Double applyScroll(String text, double amount)
    {
        return Mth.clamp((int) (parseValue(text) + amount * scrollMultiplier()), min, max);
    }

    public double parse()
    {
        return parseValue(text());
    }
}
