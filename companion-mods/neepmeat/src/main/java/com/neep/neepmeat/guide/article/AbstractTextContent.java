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

import com.neep.neepmeat.api.theme.GuideCols;
import com.neep.neepmeat.client.screen.tablet.ArticleTextWidget;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public abstract class AbstractTextContent implements Article.Content
{
    private final Component text;

    public AbstractTextContent(Component text)
    {
        var style = text.getStyle().withFont(GuideCols.FONT);
        this.text = text.copy().setStyle(style);
    }

    @Override
    public int height(float width, ArticleTextWidget parent)
    {
        return getLines(parent, width).size() * parent.getTextRenderer().lineHeight;
    }

    @Override
    public void search(String term, IntConsumer result, float width, ArticleTextWidget parent)
    {
        int h = 0;
        for (var entry : getLines(parent, width))
        {
            StringBuilder sb = new StringBuilder();
            entry.accept((index, style, codePoint) ->
            {
                sb.appendCodePoint(codePoint);
                return true;
            });

            if (sb.toString().toLowerCase(Locale.ROOT).contains(term))
                result.accept(h);

            h += parent.getTextRenderer().lineHeight;
        }
    }

    protected List<FormattedCharSequence> getLines(ArticleTextWidget parent, float width)
    {
        Font renderer = parent.getTextRenderer();
        return renderer.split(text, (int) width - 1);
    }
}
