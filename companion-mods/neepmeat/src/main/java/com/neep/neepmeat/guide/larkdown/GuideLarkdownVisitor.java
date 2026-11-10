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

package com.neep.neepmeat.guide.larkdown;

import com.neep.meatlib.larkdown.content.*;
import com.neep.meatlib.larkdown.LarkdownContent;
import com.neep.meatlib.larkdown.LarkdownParser;
import com.neep.neepmeat.client.screen.tablet.graph.GraphRegistry;
import com.neep.neepmeat.guide.article.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/// Converts larkdown content into guide article content
public class GuideLarkdownVisitor implements LarkdownParser.Visitor
{
    private final List<Article.Content> contents;

    private final LarkdownParser.Visitor delegate = LarkdownParser.Visitor.builder()
            .type(BulletLarkdownContent.class, this::visit1)
            .type(HeadingLarkdownContent.class, this::visit1)
            .type(TextLarkdownContent.class, this::visit1)
            .type(CodeLarkdownContent.class, this::visit1)
            .type(ImageLarkdownContent.class, this::visit1)
            .type(GraphLarkdownContent.class, this::visit1)
            .type(ColumnsLarkdownContent.class, this::visit1)
            .type(ItemRenderLarkdownContent.class, this::visit1)
            .build();

    public GuideLarkdownVisitor(List<LarkdownContent> list)
    {
        this.contents = new ObjectArrayList<>();

        list.forEach(this::visit);
    }

    public void visit1(BulletLarkdownContent content)
    {
        contents.add(new TextContent(Component.nullToEmpty("- " + content.text()).copy()));
    }

    public void visit1(HeadingLarkdownContent content)
    {
        if (content.level() == 1)
            contents.add(new CenteredTextContent(Component.nullToEmpty("--- " + content.text() + " ---").copy().withStyle(ChatFormatting.BOLD)));
        else
            contents.add(new TextContent(Component.nullToEmpty(content.text()).copy().withStyle(ChatFormatting.BOLD)));
    }

    public void visit1(TextLarkdownContent content)
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

        contents.add(switch (content.alignment())
        {
            case CENTRE -> new CenteredTextContent(mutableText);
            default -> new TextContent(mutableText);
        });
    }

    public void visit1(CodeLarkdownContent content)
    {
        StringBuilder sb = new StringBuilder();
        for (var line : content.lines())
        {
            sb.append(line);
            sb.append('\n');
        }

        contents.add(new CodeContent(Component.nullToEmpty(sb.toString()).copy()));
    }

    public void visit1(ImageLarkdownContent content)
    {
        ResourceLocation id = ResourceLocation.tryParse(content.path());
        ImageContent imageContent = new ImageContent(content.width(), content.height(), content.scale(), id);
        contents.add(imageContent);
    }

    public void visit1(GraphLarkdownContent content)
    {
        var id = content.id();
        GraphContent graphContent = new GraphContent(() -> GraphRegistry.get(id));
        contents.add(graphContent);
    }

    public void visit1(ColumnsLarkdownContent content)
    {
        var leftVisitor = new GuideLarkdownVisitor(content.left());
        var rightVisitor = new GuideLarkdownVisitor(content.right());
        contents.add(new ColumnContent(leftVisitor.contents, rightVisitor.contents, content.fit()));
    }

    public void visit1(ItemRenderLarkdownContent content)
    {
        contents.add(new ItemRenderContent(content));
    }

    public Article makeArticle()
    {
        return new Article.Simple(contents);
    }

    @Override
    public void visit(LarkdownContent content)
    {
        delegate.visit(content);
    }
}
