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

package com.neep.neepmeat.thord.documentation;

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepmeat.documentation.DocumentationReloadListener;
import com.neep.neepmeat.client.screen.tablet.ArticleTextWidget;
import com.neep.neepmeat.client.screen.tablet.GuideScreen;
import com.neep.neepmeat.guide.GuideNode;
import com.neep.neepmeat.guide.GuideReloadListener;
import com.neep.neepmeat.guide.article.Article;
import com.neep.neepmeat.init.NMDocs;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ThordWordArticle implements Article
{
    private List<CategoryContent> contents = null; // Lazily initialise to prevent race conditions when reloading
    private final GuideNode.MenuNode menuNode; // Placeholder menu node

    public ThordWordArticle(GuideNode.MenuNode menuNode)
    {
        this.menuNode = menuNode;
        menuNode.onVisit = s ->
        {
            contents();
            GuideReloadListener.getInstance().setPersistentArticle(this);
            s.openArticle(this);
        };
    }

    @Override
    public Iterable<? extends Content> contents()
    {
        if (contents == null)
        {
            contents = new ObjectArrayList<>();
            var categories = NMDocs.THORD_WORD_DOCS.getEntries().stream().toList();
            for (var category : categories)
            {
                // NOt sure why
                if (category.contents().isEmpty())
                    continue;

                var categoryContent = new CategoryContent(category);
                contents.add(categoryContent);

                menuNode.addChild(new CategoryNode(category));

                for (int i = 0; i < category.contents().size(); i++)
                {
                    var content = category.contents().get(i);
                    menuNode.addChild(new DescriptionNode(category, content, this));
                }
            }
        }

        return contents;
    }

    private int scrollFor(DocumentationReloadListener.Description description, float width, ArticleTextWidget parent)
    {
        int h = 0;
        for (var c : contents())
        {
            if (c instanceof CategoryContent categoryContent)
            {
                int s = categoryContent.scrollFor(description, width, parent);
                if (s != -1)
                    return h + s;

                h += categoryContent.height(width, parent);
            }
            else
            {
                h += c.height(width, parent);
            }
        }

        return 0;
    }

    private record CategoryNode(DocumentationReloadListener.Category category) implements GuideNode.Simple
    {
        @Override
        public String getId()
        {
            return category.id();
        }

        @Override
        public Component getText()
        {
            return Component.nullToEmpty("--- " + category.name() + " ---");
        }

        @Override
        public void visitScreen(GuideScreen screen) { }

        @Override
        public boolean section()
        {
            return true;
        }

        public ResourceLocation getIcon() { return ResourceLocation.parse("air"); }
    }

    private record DescriptionNode(String prefix, DocumentationReloadListener.Description content, ThordWordArticle article) implements GuideNode.Simple
    {
        public DescriptionNode(DocumentationReloadListener.Category category, DocumentationReloadListener.Description description, ThordWordArticle article)
        {
            this(category.id(), description, article);
        }

        @Override
        public String getId()
        {
            return prefix + content.name();
        }

        @Override
        public Component getText()
        {
            return Component.nullToEmpty(content.name());
        }

        @Override
        public void visitScreen(GuideScreen screen)
        {
            var pane = screen.openArticle(article);
            int scroll = article.scrollFor(content, pane.regionWidth(), pane);
            screen.setArticleScroll(scroll);
        }

        public ResourceLocation getIcon() { return ResourceLocation.parse("air"); }
    }

    private record CategoryContent(DocumentationReloadListener.Category entry) implements Article.Content
    {
        @Override
        public int render(GuiGraphics matrices, float x, float y, float width, double scroll, ArticleTextWidget parent, float mx, float my)
        {
            int h = 0;

            // Render category name
            if (!entry.name().isEmpty())
            {
                Font renderer = parent.getTextRenderer();

                Font textRenderer = parent.getTextRenderer();
                if (y > parent.getTop(10) && (y + renderer.lineHeight) < parent.getBottom(10))
                {
                    GUIUtil.drawCenteredText(matrices, textRenderer, Component.nullToEmpty("--- " + entry.name() + " ---").copy().withStyle(ChatFormatting.BOLD), x + width / 2, (float) (y - scroll), 0x00FF00, false);
                }

                h += textRenderer.lineHeight * 2;
            }

            for (var desc : entry.contents())
            {
                float prevH = h;

                for (var sub : desc.contents())
                {
                    h += sub.render(matrices, x, y + h + (float) scroll, width, 0, parent, mx, my);
                }

                matrices.pose().pushPose();
                matrices.pose().translate(0, y + prevH - scroll - 5, 0);
                GUIUtil.drawHorizontalLine1(matrices, (int) x - 1, (int) (x + width), 0, 0xFF888800);
                matrices.pose().translate(0, h - prevH, 0);
                GUIUtil.drawHorizontalLine1(matrices, (int) x - 1, (int) (x + width), 0, 0xFF888800);
                matrices.pose().popPose();
            }

            return h;
        }

        @Override
        public int height(float width, ArticleTextWidget parent)
        {
            int h = 0;

            if (!entry.name().isEmpty())
                h += parent.getTextRenderer().lineHeight * 2;

            for (var desc : entry.contents())
            {
                for (var sub : desc.contents())
                {
                    h += sub.height(width, parent);
                }
            }

            return h;
        }

        @Override
        public void search(String term, IntConsumer result, float width, ArticleTextWidget parent)
        {
            if (entry.name().contains(term))
                result.accept(0);

            AtomicInteger h = new AtomicInteger(0);

//            if (!entry.name().isEmpty())
//                h.addAndGet(parent.getTextRenderer().fontHeight * 2);

            for (var desc : entry.contents())
            {
                for (var sub : desc.contents())
                {
                    IntConsumer result1 = i -> result.accept(h.get());
                    sub.search(term, result1, width, parent);
                    h.addAndGet(sub.height(width, parent));
                }
            }
        }

        private int scrollFor(DocumentationReloadListener.Description content, float width, ArticleTextWidget parent)
        {
            int h = 0;
            for (var desc : entry.contents())
            {
                if (desc.equals(content))
                    return h;

                for (var sub : desc.contents())
                {
                    h += sub.height(width, parent);
                }
            }
            return -1;
        }
    }

    private static class DescriptionArticle implements Article, Article.Content
    {
        private final DocumentationReloadListener.Description description;

        private DescriptionArticle(DocumentationReloadListener.Description description)
        {
            this.description = description;
        }

        @Override
        public Iterable<? extends Content> contents()
        {
            return List.of(this);
        }


        @Override
        public int render(GuiGraphics matrices, float x, float y, float width, double scroll, ArticleTextWidget parent, float mx, float my)
        {
            int h = 0;
            float prevH = h;

            for (var sub : description.contents())
            {
                h += sub.render(matrices, x, y + h + (float) scroll, width, 0, parent, mx, my);
            }

            matrices.pose().pushPose();
            matrices.pose().translate(0, y + prevH - scroll - 5, 0);
            GUIUtil.drawHorizontalLine1(matrices, (int) x - 1, (int) (x + width), 0, 0xFF888800);
            matrices.pose().translate(0, h - prevH, 0);
            GUIUtil.drawHorizontalLine1(matrices, (int) x - 1, (int) (x + width), 0, 0xFF888800);
            matrices.pose().popPose();

            return h;
        }

        @Override
        public int height(float width, ArticleTextWidget parent)
        {
            int h = 0;
            for (var sub : description.contents())
            {
                h += sub.height(width, parent);
            }
            return h;
        }

        @Override
        public void search(String term, IntConsumer result, float width, ArticleTextWidget parent)
        {
        }
    }
}
