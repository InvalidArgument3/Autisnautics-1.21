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

package com.neep.neepmeat.client.screen.tablet;

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.auto.EndpointPanel;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.theme.GuideCols;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GuideSearchWidget extends EndpointPanel
{
    private final Component searchMessage;
    private final BooleanSupplier searchMode;
    private final Font textRenderer;

    public final StringBuilder string = new StringBuilder();

    public GuideSearchWidget(String prefix, BooleanSupplier searchMode, Font textRenderer)
    {
        searchMessage = NeepMeat.translationKey("screen", "guide.search", prefix).setStyle(GuideCols.style());
        this.searchMode = searchMode;
        this.textRenderer = textRenderer;
    }

    protected void write(char chr, int modifiers)
    {
        string.append(Character.toLowerCase(chr));
    }

    public void erase(int dist)
    {
        if (string.isEmpty())
            return;

        string.delete(string.length() - 1, string.length());
    }

    public void clear()
    {
        string.setLength(0);
    }

    public String toString()
    {
        return string.toString();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        if (!searchMode.getAsBoolean())
        {
            GUIUtil.drawText(context, textRenderer, searchMessage, this.x0() + 2, this.y0() + Math.round((nh() - 9) / 2f), 0x008800, false);
        }
        else
        {
            GUIUtil.drawText(context, textRenderer, "?" + toString(), this.x0() + 2, this.y0() + Math.round((nh() - 9) / 2f), 0x008800, false);
        }
    }
}
