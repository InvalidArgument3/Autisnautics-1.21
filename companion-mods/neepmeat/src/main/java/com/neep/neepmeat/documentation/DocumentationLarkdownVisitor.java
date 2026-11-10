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

package com.neep.neepmeat.documentation;

import com.neep.meatlib.larkdown.LarkdownContent;
import com.neep.meatlib.larkdown.LarkdownParser;
import com.neep.meatlib.larkdown.content.*;
import com.neep.neepmeat.guide.article.Article;
import com.neep.neepmeat.guide.article.CodeContent;
import com.neep.neepmeat.guide.article.TextContent;
import com.neep.neepmeat.thord.documentation.CategoryLarkdownContent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

class DocumentationLarkdownVisitor implements LarkdownParser.Visitor
{
    private List<Article.Content> contents = new ObjectArrayList<>();

    private final LarkdownParser.Visitor delegate = LarkdownParser.Visitor.builder()
            .type(BulletLarkdownContent.class, this::visit1)
            .type(CategoryLarkdownContent.class, this::visit1)
            .type(HeadingLarkdownContent.class, this::visit1)
            .type(TextLarkdownContent.class, this::visit1)
            .type(CodeLarkdownContent.class, this::visit1)
            .type(ImageLarkdownContent.class, this::visit1)
            .build();

    public DocumentationLarkdownVisitor()
    {
    }

    public List<Article.Content> apply(List<LarkdownContent> contents)
    {
        this.contents = new ObjectArrayList<>();

        for (LarkdownContent content : contents)
        {
            visit(content);
        }

        return this.contents;
    }

    private void visit1(CategoryLarkdownContent content)
    {
//        contents.add(new TextContent(Text.of(content.name()).copy().formatted(Formatting.BOLD)));
    }

    private void visit1(BulletLarkdownContent content)
    {
        contents.add(new TextContent(Component.nullToEmpty("- " + content.text())));
    }

    private void visit1(HeadingLarkdownContent content)
    {
        contents.add(new TextContent(Component.nullToEmpty(content.text()).copy().withStyle(ChatFormatting.BOLD)));
    }

    private void visit1(TextLarkdownContent content)
    {
        MutableComponent mutableText = Component.empty().copy();
        for (var entry : content.entries())
        {
            Component text = switch (entry.format())
            {
                case NONE -> Component.nullToEmpty(entry.text());
                case BOLD -> Component.literal(entry.text()).withStyle(ChatFormatting.BOLD);
                case ITALIC -> Component.literal(entry.text()).withStyle(ChatFormatting.ITALIC);
                case CODE -> Component.literal(entry.text()).withStyle(ChatFormatting.ITALIC); // TODO: embed mono text in normal text
                case STRIKETHROUGH -> Component.literal(entry.text()).withStyle(ChatFormatting.STRIKETHROUGH);
            };

            mutableText.append(text);
        }

        contents.add(new TextContent(mutableText));
    }

    private void visit1(CodeLarkdownContent content)
    {
        StringBuilder sb = new StringBuilder();
        for (var line : content.lines())
        {
            sb.append(line);
            sb.append('\n');
        }

        contents.add(new CodeContent(Component.nullToEmpty(sb.toString()).copy()));
    }

    private void visit1(ImageLarkdownContent content)
    {
    }

    @Override
    public void visit(LarkdownContent content)
    {
        delegate.visit(content);
    }
}
