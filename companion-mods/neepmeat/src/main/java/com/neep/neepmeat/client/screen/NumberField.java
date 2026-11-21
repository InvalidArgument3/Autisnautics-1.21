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

package com.neep.neepmeat.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public class NumberField extends com.neep.meatlib.client.screen.NMTextField
{
    public NumberField(Font textRenderer, int x, int y, int width, int height, Component text)
    {
        super(textRenderer, x, y, width, height, text);
    }

    @Override
    public void write(String text)
    {
        if (!text.matches("[0-9]*")) return;
        super.write(text);
    }
}
