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

import com.neep.meatlib.screen.MeatlibScreenHandlers;
import com.neep.meatlib.theme.PLCCols;
import com.neep.meatlib.client.screen.GUIUtil;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;

public class PlayerSlotsBorder implements Renderable
{
    private final int x;
    private final int y;

    protected final Supplier<Integer> col;

    public PlayerSlotsBorder(int x, int y, Supplier<Integer> col)
    {
        this.x = x;
        this.y = y;
        this.col = col;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        renderSlotBorders(context, x, y, col.get(), PLCCols.TRANSPARENT.col);
    }

    public static void renderSlotBorders(GuiGraphics matrices, int x, int y, int col, int transparent)
    {
        int w = MeatlibScreenHandlers.playerSlotsW();
        int h = MeatlibScreenHandlers.playerInvH();
        GUIUtil.renderBorder(matrices, x, y, w - 1, h - 1 - 18 - 4, col, 0);
        GUIUtil.renderBorder(matrices, x, y, w - 1, h - 1 - 18 - 4, transparent, -1);

        GUIUtil.renderBorder(matrices, x, y + 58, w - 1, 18 + 1, col, 0);
        GUIUtil.renderBorder(matrices, x, y + 58, w - 1, 18 + 1, transparent, -1);
    }
}
