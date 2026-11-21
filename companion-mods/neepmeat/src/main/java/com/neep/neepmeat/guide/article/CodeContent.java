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

import com.neep.neepmeat.client.screen.plc.MonoTextRenderer;
import com.neep.neepmeat.client.screen.tablet.ArticleTextWidget;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

public class CodeContent implements Article.Content
{
    private final Component text;
    private static final MonoTextRenderer RENDERER = new MonoTextRenderer();

    public CodeContent(MutableComponent text)
    {
        this.text = text;
    }

    @Override
    public int render(GuiGraphics matrices, float x, float y, float width, double scroll, ArticleTextWidget parent, float mx, float my)
    {
        List<FormattedCharSequence> lines = RENDERER.wrapLines(text, (int) width);
        int fontHeight = RENDERER.fontHeight();
        int i = 0;
        while (i < lines.size())
        {
            float head = y + i * fontHeight;
            if (head > parent.getTop(10) && (head + fontHeight) < parent.getBottom(10))
            {
                RENDERER.draw(matrices.pose(), lines.get(i), x + 5, (float) (y - scroll + i * fontHeight), parent.getCodeCol());
            }
            ++i;
        }
        return i * fontHeight;
    }

    @Override
    public int height(float width, ArticleTextWidget parent)
    {
        List<FormattedCharSequence> lines = RENDERER.wrapLines(text, (int) width);
        return lines.size() * RENDERER.fontHeight();
    }

    @Override
    public void search(String term, IntConsumer result, float width, ArticleTextWidget parent)
    {
        if (text.getString().toLowerCase(Locale.ROOT).contains(term))
            result.accept(0);
    }
}
