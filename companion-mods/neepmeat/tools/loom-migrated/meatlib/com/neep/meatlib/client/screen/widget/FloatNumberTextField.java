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
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class FloatNumberTextField extends AbstractNumberTextField<Float> implements Rectangle.Mutable
{
    private final Consumer<FloatNumberTextField> update;
    private final float min;
    private final float max;

    private final float minIncrement;
    private final float maxIncrement;

    public FloatNumberTextField(Font textRenderer, int x, int y, int width, int height, Component prefix, Consumer<FloatNumberTextField> update,
                                float min, float max)
    {
        this(textRenderer, x, y, width, height, prefix, update, min, max, 1, 16);
    }

    public FloatNumberTextField(Font textRenderer, int x, int y, int width, int height, Component prefix, Consumer<FloatNumberTextField> update,
                                float min, float max, float minIncrement, float maxIncrement)
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
        return text.matches("[+\\-0-9E.]*");
    }

    @Override
    protected String valueToString(Float value)
    {
        return FormatUtil.formatFloat(value, true);
    }

    protected float parseValue(String text)
    {
        try
        {
            return Mth.clamp(Float.parseFloat(text), min, max);
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
    protected Float applyScroll(String text, double amount)
    {
        return Mth.clamp((parseValue(text) + (float) amount * scrollMultiplier()), min, max);
    }

    public float parse()
    {
        return parseValue(text());
    }
}
