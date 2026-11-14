package com.neep.neepmeat.guide.article;

import com.neep.neepmeat.client.screen.tablet.ArticleTextWidget;
import com.neep.neepmeat.guide.larkdown.ColumnsLarkdownContent;
import java.util.List;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphics;

public record ColumnContent(List<Article.Content> left, List<Article.Content> right, ColumnsLarkdownContent.Fit fit) implements Article.Content
{
    @Override
    public int render(GuiGraphics matrices, float x, float y, float width, double scroll, ArticleTextWidget parent, float mx, float my)
    {
//        width -= 3;
        float divide = getDivide(width);
        float lw = divide * width;
        float rw = (1 - divide) * width;

        float h1 = renderColumn(matrices, x, y, lw, parent, left, mx, my);
        float h2 = renderColumn(matrices, x + lw, y, rw, parent, right, mx, my);

        return (int) Math.max(h1, h2);
    }

    public float getDivide(float width)
    {
        if (fit == ColumnsLarkdownContent.Fit.MIDDLE)
            return 0.5f;

        else if (fit == ColumnsLarkdownContent.Fit.FIRST)
        {
            float max = 0;
            for (var c : left)
            {
                if (c instanceof SolidW solid)
                {
                    max = Math.max(max, solid.desiredWidth(width / 2));
                }
            }
            return max > 0 ? Math.min(width / 2, max) / width : 0.5f;
        }
        else
        {
            float max = 0;
            for (var c : right)
            {
                if (c instanceof SolidW solid)
                {
                    max = Math.max(max, solid.desiredWidth(width / 2));
                }
            }
            return max > 0 ? 1 - Math.min(width / 2, max) / width : 0.5f;
        }
    }

    private static float renderColumn(GuiGraphics c, float x, float y, float width, ArticleTextWidget parent, List<Article.Content> contents, float mx, float my)
    {
        float h = 0;
        for (var content : contents)
        {
            h += content.render(c, x, y + h, width, 0, parent, mx, my);
        }
        return h;
    }

    private static float height(float width, ArticleTextWidget parent, List<Article.Content> contents)
    {
        return (float) contents.stream().mapToDouble(c -> c.height(width, parent)).sum();
    }

    @Override
    public boolean onClick(float x, float y, float width, double scroll, ArticleTextWidget parent, float mx, float my)
    {
        int head = 0;

        float divide = getDivide(width);
        float lw = divide * width;
        float rw = (1 - divide) * width;

        for (Article.Content content : left)
        {
            if (content.onClick(x, y + head, lw, 0, parent, mx, my))
                return true;

            head += content.height(lw, parent);
        }

        head = 0;
        for (Article.Content content : right)
        {
            if (content.onClick(x + lw, y + head, rw, 0, parent, mx, my))
                return true;

            head += content.height(lw, parent);
        }

        return Article.Content.super.onClick(x, y, width, scroll, parent, mx, my);
    }

    @Override
    public int height(float width, ArticleTextWidget parent)
    {
        return (int) Math.max(height(width, parent, left), height(width, parent, right));
    }

    @Override
    public void search(String term, IntConsumer result, float width, ArticleTextWidget parent)
    {

    }
}
