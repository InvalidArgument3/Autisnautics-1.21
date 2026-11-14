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

package com.neep.neepmeat.client.screen.plc;

import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.plc.screen.PLCScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;

public class CurrentArgumentWidget implements Renderable
{
    private final int x;
    private final int y;
    private final PLCScreenHandler handler;
    private final Font textRenderer = Minecraft.getInstance().font;

    public CurrentArgumentWidget(int x, int y, PLCScreenHandler handler)
    {
        this.x = x;
        this.y = y;

        this.handler = handler;
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        Component text = Component.translatable("text." + NeepMeat.NAMESPACE + ".plc.arguments", handler.getArguments(), handler.getMaxArguments());

        matrices.drawString(textRenderer, text, x, y, PLCCols.TEXT.col, true);
    }
}
