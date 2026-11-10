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

package com.neep.meatlib.theme;


import net.minecraft.network.chat.Style;

// I use the name 'col' because I don't want to give in and start using 'color'
public enum PLCCols
{
    // No idea why I used an enum instead of static final ints
    ERROR_LINE(rgba(200, 30, 30, 100)),
    INVALID(rgba(100, 30, 30, 255)),
    BORDER(rgba(255, 94, 33, 255)),
    TEXT(rgba(255, 94, 33, 255)),
    SELECTED(rgba(255, 150, 33, 255)),
    DEBUG_LINE(rgba(120, 10, 100, 100)),
    TRANSPARENT(rgba(255, 94, 33, 100)),
    LINE_NUMBER(rgba(200, 94, 33, 255))
    ;


    public static int rgba(int r, int g, int b, int a)
    {
        return a << 24 | r << 16 | g << 8 | b;
    }

    PLCCols(int col)
    {
        this.col = col;
    }

    public final int col;

    public static final Style NORMAL_TEXT = Style.EMPTY.withColor(PLCCols.TEXT.col);
    public static final Style EMPHASIS_TEXT = Style.EMPTY.withColor(PLCCols.SELECTED.col);

    public static final int
            ENTRY_H = 16,
            INV_W = 176,
            INV_H = 90
            ;

    public static final int
            MEAT_ITEM_BAR_COL = 0xbb0000
            ;
}
