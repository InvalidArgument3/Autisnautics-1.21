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

package com.neep.neepmeat.guide.article;

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepmeat.client.screen.tablet.ArticleTextWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TextContent extends AbstractTextContent
{
    public TextContent(Component text)
    {
        super(text);
    }

    @Override
    public int render(GuiGraphics matrices, float x, float y, float width, double scroll, ArticleTextWidget parent, float mx, float my)
    {
        int i = 0;
        var lines = getLines(parent, width);

        int fontHeight = parent.getTextRenderer().lineHeight;

        while (i < lines.size())
        {
            float head = y + i * fontHeight;
            if (head > parent.getTop(10) && (head + fontHeight) < parent.getBottom(10))
            {
                GUIUtil.drawText(matrices, parent.getTextRenderer(), lines.get(i), x, (float) (y - scroll + i * fontHeight), parent.getCol(), false);
            }
            ++i;
        }
        return i * fontHeight;
    }
}
