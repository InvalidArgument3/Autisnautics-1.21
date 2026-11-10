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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.neepmeat.client.screen.tablet.ArticleTextWidget;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class ImageContent implements Article.Content
{
//    private final Text caption;
    private final int width;
    private final int height;
    private final float scale;
    private final ResourceLocation image;

    public ImageContent(int width, int height, float scale, ResourceLocation image)
    {
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.image = image;
    }

    @Override
    public int render(GuiGraphics context, float x, float y, float width, double scroll, ArticleTextWidget parent, float mx, float my)
    {
        PoseStack matrices = context.pose();
        matrices.pushPose();
        RenderSystem.setShaderTexture(0, image);

        float scaledWidth = width * scale;
        float scaledHeight = this.height * scaledWidth / this.width;

        float cx = x + (width / 2 - scaledWidth / 2f);
        float i = Math.max(0, parent.getTop(10) - y);
        float j = Math.max(0, y + scaledHeight - parent.getBottom(10));
        context.blit(image,
                (int) cx,
                (int) ((int) y - scroll + i),
                0,
                0, i,
                (int) scaledWidth,
                (int) (scaledHeight - i - j),
                (int) scaledWidth, (int) scaledHeight);
        matrices.popPose();

        return (int) scaledHeight;
    }

    @Override
    public int height(float width, ArticleTextWidget parent)
    {
        float scaledWidth = width * scale;
        return (int) (this.height * scaledWidth / this.width);
    }

    @Override
    public void search(String term, IntConsumer result, float width, ArticleTextWidget parent)
    {
    }
}
