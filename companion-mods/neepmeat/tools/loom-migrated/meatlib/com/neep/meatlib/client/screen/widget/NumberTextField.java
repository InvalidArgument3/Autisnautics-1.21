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

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/**
 * Convenience implementation
 */
public class NumberTextField<T extends Number> extends AbstractNumberTextField<T>
{
    private final Runnable update;
    private final Predicate<String> isValidString;
    private final Function<T, String> valueToString;
    private final BiFunction<String, Double, T> scroll;

    public NumberTextField(Font textRenderer, int x, int y, int width, int height, Component prefix,
                           Runnable update, Predicate<String> isValidString, Function<T, String> valueToString, BiFunction<String, Double, T> scroll)
    {
        super(textRenderer, x, y, width, height, prefix);
        this.update = update;
        this.isValidString = isValidString;
        this.valueToString = valueToString;
        this.scroll = scroll;
    }

    @Override
    protected void update()
    {
        update.run();
    }

    @Override
    protected boolean isValidString(String text)
    {
        return isValidString.test(text);
    }

    @Override
    protected String valueToString(T value)
    {
        return valueToString.apply(value);
    }

    @Override
    protected T applyScroll(String prevText, double amount)
    {
        return scroll.apply(prevText, amount);
    }
}
