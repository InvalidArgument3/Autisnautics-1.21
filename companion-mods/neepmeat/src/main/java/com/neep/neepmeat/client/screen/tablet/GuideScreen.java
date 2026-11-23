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

import com.neep.neepmeat.guide.GuideNode;
import com.neep.neepmeat.guide.article.Article;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;

public interface GuideScreen
{
    void setRightPane(ContentPane element);
    void push(GuideNode node);
    GuideNode pop();
    Deque<GuideNode> getPath();

    int getAnimationTicks();

    ArticleTextWidget openArticle(Article article);
    void setArticleScroll(int scroll);

    ContentPane getRightPane();
}
