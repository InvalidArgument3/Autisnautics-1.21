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

package com.neep.neepmeat.guide;

import com.neep.neepmeat.client.screen.tablet.GuideScreen;
import com.neep.neepmeat.guide.article.Article;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public interface GuideNode
{
    List<GuideNode> getChildren();
    void addChild(GuideNode node);
    String getId();
    ResourceLocation getIcon();
    Component getText();
    void visitScreen(GuideScreen screen);
    default boolean section() { return false; }

    interface Simple extends GuideNode
    {
        default List<GuideNode> getChildren() { return Collections.emptyList(); }

        @Override
        default void addChild(GuideNode node) { }
    }

    GuideNode BACK = new GuideNode()
    {
        @Override
        public List<GuideNode> getChildren()
        {
            return Collections.emptyList();
        }

        @Override
        public void addChild(GuideNode node) {}

        @Override
        public String getId()
        {
            return "";
        }

        @Override
        public ResourceLocation getIcon()
        {
            return ResourceLocation.parse("minecraft:empty");
        }

        @Override
        public Component getText()
        {
            return Component.nullToEmpty(" \u2190");
        }

        @Override
        public void visitScreen(GuideScreen screen)
        {
            screen.pop();
        }
    };

    abstract class GuideNodeImpl implements GuideNode
    {
        private final String id;
        private final ResourceLocation icon;
        private final Component text;

        public GuideNodeImpl(String id, ResourceLocation icon, Component text)
        {
            this.id = id;
            this.icon = icon;
            this.text = text;
        }

        @Override
        public String getId()
        {
            return id;
        }

        @Override
        public ResourceLocation getIcon()
        {
            return icon;
        }

        @Override
        public Component getText()
        {
            return text;
        }

        @Override
        public boolean equals(Object other)
        {
            if (other instanceof GuideNode node)
            {
                return Objects.equals(node.getId(), getId());
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            return id.hashCode();
        }
    }

    class MenuNode extends GuideNodeImpl
    {
        private final List<GuideNode> children;
        public Consumer<GuideScreen> onVisit = s -> {};

        public MenuNode(String id, ResourceLocation icon, Component text, List<GuideNode> children)
        {
            super(id, icon, text);
            this.children = children;
        }

        @Override
        public List<GuideNode> getChildren()
        {
            return children;
        }

        @Override
        public void addChild(GuideNode node)
        {
            children.add(node);
        }

        @Override
        public void visitScreen(GuideScreen screen)
        {
            onVisit.accept(screen);
            screen.push(this);
        }
    }

    class ArticleNode extends GuideNodeImpl
    {
        private final Set<String> lookups;

        public ArticleNode(String id, Set<String> lookup, ResourceLocation icon, Component text)
        {
            super(id, icon, text);
            this.lookups = new HashSet<>(lookup);
        }

        @Override
        public List<GuideNode> getChildren()
        {
            return Collections.emptyList();
        }

        @Override
        public void addChild(GuideNode node)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitScreen(GuideScreen screen)
        {
            Article article = GuideReloadListener.getInstance().getArticle(getId());
            GuideReloadListener.getInstance().setPersistentArticle(article);
            screen.openArticle(article);
        }

        public boolean matchesLookupTerm(String term)
        {
            return this.lookups.contains(term);
        }

        public void addLookupTerm(String term)
        {
            lookups.add(term);
        }
    }
}
