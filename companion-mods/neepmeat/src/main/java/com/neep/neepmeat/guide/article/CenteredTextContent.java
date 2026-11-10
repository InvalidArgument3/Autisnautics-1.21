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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

public class CenteredTextContent extends AbstractTextContent
{
    public CenteredTextContent(MutableComponent text)
    {
        super(text);
    }

    @Override
    public int render(GuiGraphics matrices, float x, float y, float width, double scroll, ArticleTextWidget parent, float mx, float my)
    {
        int i = 0;
        var renderer = parent.getTextRenderer();
        var lines = getLines(parent, width);
        while (i < lines.size())
        {
            float head = y + i * renderer.lineHeight;
            if (head > parent.getTop(10) && (head + renderer.lineHeight) < parent.getBottom(10))
            {
                FormattedCharSequence line = lines.get(i);
                float x1 = x + width / 2 - renderer.width(line) / 2f;
                GUIUtil.drawText(matrices, parent.getTextRenderer(), line, x1, (float) (y - scroll + i * renderer.lineHeight), parent.getCol(), false);
            }
            ++i;
        }
        return i * renderer.lineHeight;
    }
}
