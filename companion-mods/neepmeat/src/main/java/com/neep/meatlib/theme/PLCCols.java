/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.theme;

import net.minecraft.network.chat.Style;

public enum PLCCols {
    ERROR_LINE(rgba(200, 30, 30, 100)),
    INVALID(rgba(100, 30, 30, 255)),
    BORDER(rgba(255, 94, 33, 255)),
    TEXT(rgba(255, 94, 33, 255)),
    SELECTED(rgba(255, 150, 33, 255)),
    DEBUG_LINE(rgba(120, 10, 100, 100)),
    TRANSPARENT(rgba(255, 94, 33, 100)),
    LINE_NUMBER(rgba(200, 94, 33, 255));

    public static int rgba(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }

    PLCCols(int col) {
        this.col = col;
    }

    public final int col;

    public static final Style NORMAL_TEXT = Style.EMPTY.withColor(PLCCols.TEXT.col);
    public static final Style EMPHASIS_TEXT = Style.EMPTY.withColor(PLCCols.SELECTED.col);

    public static final int ENTRY_H = 16, INV_W = 176, INV_H = 90;
    public static final int MEAT_ITEM_BAR_COL = 0xbb0000;
}
