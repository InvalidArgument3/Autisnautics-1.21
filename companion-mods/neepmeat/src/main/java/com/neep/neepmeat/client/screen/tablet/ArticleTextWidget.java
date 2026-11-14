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
import com.neep.meatlib.client.screen.TooltipHelper;
import com.neep.meatlib.client.screen.primitive.Point;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.neepmeat.api.theme.GuideCols;
import com.neep.neepmeat.guide.article.Article;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class ArticleTextWidget implements Rectangle, Point.Mutable, GuiEventListener, Renderable, NarratableEntry
{
    private double scrollAmount;
    private double scrollLag;
    private int highlightedY;
    private int x, y, w, h;
    private final Font textRenderer;

    private final TooltipHelper tooltipHelper;

    record GuideTooltip(List<Component> texts, float x, float y, int col, boolean offset) {}
    @Nullable private GuideTooltip nextTooltip;

    public Article getArticle()
    {
        return article;
    }

    private final Article article;

    private float articleLength = 0;

    private boolean searchMode = false;
    private final GuideSearchWidget searchString;
    private IntList searchResult = new IntArrayList();

    public ArticleTextWidget(Font textRenderer, Article article)
    {
        this.textRenderer = textRenderer;
        this.article = article != null ? article : Article.EMPTY;
        this.searchString = new GuideSearchWidget("?", () -> this.searchMode, this.textRenderer);
        tooltipHelper = new TooltipHelper(true, getCol(), false, this.textRenderer);
    }

    public void setDimensions(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.w = width;
        this.h = height;
    }

    public Font getTextRenderer()
    {
        return textRenderer;
    }

    public ItemRenderer getItemRenderer()
    {
        return Minecraft.getInstance().getItemRenderer();
    }

    public int getTop(int margin)
    {
        return (int) y - margin;
    }

    public int getBottom(int margin)
    {
        return (int) (y + h) + margin;
    }

    public void setNextTooltip(List<Component> tooltip, float x, float y, int col, boolean offset)
    {
        this.nextTooltip = new GuideTooltip(tooltip, x, y, col, offset);
    }

    public Player getPlayer()
    {
        return Minecraft.getInstance().player;
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        scrollAmount = Mth.lerp(0.4, scrollAmount, scrollLag);
        int head = 0;

        searchString.setDims(x, y + h - textRenderer.lineHeight, w, textRenderer.lineHeight);

        matrices.enableScissor(x, y, x + w, y + contentHeight());
        if (highlightedY >= 0 && !searchResult.isEmpty())
        {
            int highlightOffset = (int) (y + highlightedY - scrollAmount) + 1;
            GUIUtil.fill(matrices, x, highlightOffset, x + w, highlightOffset + textRenderer.lineHeight + 1, 0x4400FF00);
        }

        for (Article.Content content : article.contents())
        {
            head += content.render(matrices, x + 2, y - (int) scrollAmount + 2 + head, w, 0, this, mouseX, mouseY);
        }
        matrices.disableScissor();

        searchString.render(matrices, mouseX, mouseY, delta);

        if (nextTooltip != null)
        {
            List<Component> wrapped = new ObjectArrayList<>();
            nextTooltip.texts.forEach(t -> TooltipSupplier.wrapLines(wrapped, t));
            tooltipHelper.col = nextTooltip.col;
            tooltipHelper.offset = nextTooltip.offset;
            tooltipHelper.draw(matrices, (int) nextTooltip.x, (int) nextTooltip.y, wrapped);
            nextTooltip = null;
        }

        articleLength = head;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        int head = 0;
        for (Article.Content content : article.contents())
        {
            if (content.onClick(x + 2, y - (int) scrollAmount + 2 + head, w, 0, this, (float) mouseX, (float) mouseY))
                return true;

            head += content.height(w, this);
        }

        return false;
    }

    @Override
    public NarrationPriority narrationPriority()
    {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput builder)
    {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (searchMode)
        {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE)
            {
                clearSearch();
                searchMode = false;
                return true;
            }
            else if (keyCode == GLFW.GLFW_KEY_BACKSPACE)
            {
                searchString.erase(-1);
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            searchMode = false;
            updateSearch();
            scrollToNextResult();
            return true;
        }

        return GuiEventListener.super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        if (!searchMode && chr == '?')
        {
            searchMode = true;
        }
        else if (searchMode)
        {
            searchString.write(chr, modifiers);
            return true;
        }

        else
        {
            if (chr == 'n')
            {
                scrollToNextResult();
                return true;
            }
        }

        return GuiEventListener.super.charTyped(chr, modifiers);
    }

    private void scrollToNextResult()
    {
        if (searchResult.isEmpty())
            return;

        for (var entry : searchResult)
        {
            if (entry > highlightedY)
            {
                highlightedY = entry;

                if (highlightedY < scrollLag)
                    setScroll(entry);
                else if (highlightedY > scrollLag + contentHeight())
                    setScroll(highlightedY - 0.75 * contentHeight());

                return;
            }
        }

        // Reached end of results. Wrap.
        highlightedY = searchResult.getInt(0);
        setScroll(highlightedY);
    }

    private int contentHeight()
    {
        return h - searchString.nh();
    }

    private void clearSearch()
    {
        searchString.clear();
        searchResult.clear();
    }

    private void updateSearch()
    {
        searchResult = article.search(searchString.toString().toLowerCase(Locale.ROOT), w, this);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double amount)
    {
        return true;
    }
    
    public void scroll(int amount)
    {
        setScroll(scrollLag - 20 * amount);
    }

    public void setScroll(double scroll)
    {
        scrollLag = Mth.clamp(scroll, 0, Math.max(articleLength - contentHeight() + 3, 0));
    }

    @Override
    public void setFocused(boolean focused)
    {

    }

    @Override
    public boolean isFocused()
    {
        return false;
    }

    public float regionWidth()
    {
        return w;
    }

    @Override
    public void setPos(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public int x0()
    {
        return x;
    }

    @Override
    public int y0()
    {
        return y;
    }

    @Override
    public int nw()
    {
        return w;
    }

    @Override
    public int nh()
    {
        return h;
    }

    public int getCol()
    {
        return GuideCols.textCol();
    }

    public int getDarkCol()
    {
        return GuideCols.darkCol();
    }

    public int getCodeCol()
    {
        return GuideCols.codeCol();
    }
}
