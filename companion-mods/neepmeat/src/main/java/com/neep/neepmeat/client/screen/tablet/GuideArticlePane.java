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

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.ScreenUtil;
import com.neep.neepmeat.guide.article.Article;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuideArticlePane extends ContentPane implements Renderable, GuiEventListener, NarratableEntry
{
    private final ArticleTextWidget articleWidget;

    public GuideArticlePane(GuideScreen parent, Article article)
    {
        super(parent);
        articleWidget = new ArticleTextWidget(textRenderer, article);
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        if (parent.getAnimationTicks() < 14) return;
        super.render(matrices, mouseX, mouseY, delta);
        GUIUtil.renderBorderL(matrices, x0(), this.y0(), nw(), nh(), 0xFF888800, 0);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode != GLFW.GLFW_KEY_ESCAPE)
        {
            if (ScreenUtil.keyScroll(keyCode, modifiers, articleWidget::scroll))
                return true;
        }

        return articleWidget.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        return articleWidget.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double amount)
    {
        articleWidget.scroll((int) Math.round(amount));
        return true;
    }

    public void setScroll(int scroll)
    {
        articleWidget.setScroll(scroll);
    }

    @Override
    public void init()
    {
        clearChildren();
        super.init();
        addChild(articleWidget);
        articleWidget.setDimensions(x0(), this.y0(), nw(), nh());
    }

    protected int getPageLines()
    {
        return 30;
    }

    @Override
    public void updateNarration(NarrationElementOutput builder)
    {

    }

    @Override
    public NarrationPriority narrationPriority()
    {
        return NarrationPriority.FOCUSED;
    }

    public ArticleTextWidget getWidget()
    {
        return articleWidget;
    }
}