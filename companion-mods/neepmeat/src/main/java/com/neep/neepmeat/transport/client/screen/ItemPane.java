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

package com.neep.neepmeat.transport.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.primitive.Point;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.client.screen.util.BorderScrollRight;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.ResourceAmount;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

//public class ItemPane implements Selectable, GUIUtil, ClickableWidget, Rectangle.Mutable
public class ItemPane implements ClickableWidget, Rectangle.Mutable
{
    protected final int gridColumns;
    protected final int gridRows;
    protected final ItemRenderer itemRenderer;
    protected final Font textRenderer;
    protected final List<RoutingNetwork.Entry> items;
    protected final Minecraft client;
    protected final int wSlot = 18;
    protected final int hSlot = 18;
    private final Consumer<ResourceAmount<ItemVariant>> request;
    private final SearchWidget searchWidget;
    protected int x, y;
    protected int gridStartX, gridStartY;
    protected int w, h;
    protected List<Entry> itemsToShow = List.of();
    protected int scrollRow;
    private BorderScrollRight border;
    private int scrollableRows;
    private boolean searchMode;

    record Entry(ItemVariant resource, long amount, ItemStack stack, boolean craftable, boolean real) {}

    public ItemPane(Consumer<ResourceAmount<ItemVariant>> request, int gridColumns, int gridRows, ItemRenderer itemRenderer, Font textRenderer, List<RoutingNetwork.Entry> items, Minecraft client)
    {
        this.request = request;
        this.gridColumns = gridColumns;
        this.gridRows = gridRows;

        this.itemRenderer = itemRenderer;
        this.textRenderer = textRenderer;
        this.items = items;
        this.client = client;

        this.searchWidget = new SearchWidget();

//        this.w = gridColumns * wSlot;
        this.h = gridRows * hSlot + searchWidget.nh() + 6;

        updateSearch();
    }

    public void updateSearch()
    {
        itemsToShow = items.stream()
                .map(e -> new Entry(e.resource(), e.amount(), e.resource().toStack(), e.craftable(), e.real()))
                .filter(s -> s.stack.getHoverName().getString().toLowerCase(Locale.getDefault()).contains(searchWidget.searchString))
                .toList();

        this.scrollableRows = (int) (Math.ceil((float) itemsToShow.size() / this.gridColumns) - Math.min(gridRows, Math.ceil((float) itemsToShow.size()) / gridColumns));
        scrollRow = Mth.clamp(scrollRow, 0, scrollableRows);
    }

    public void init()
    {
        gridStartX = x + (w - gridColumns * wSlot) / 2;
        gridStartY = y + 2;

        this.border = new BorderScrollRight(this.x0(), this.y0() - 1, this.w, gridRows * hSlot + 4, 0, () -> PLCCols.BORDER.col)
        {
            @Override
            public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
            {
//                GUIUtil.renderBorderInner(context, x, y, w, h, col.get(), 0);
                renderBorder(context, x, y, w - 1, h - 1, col.get(), 0, false);
                renderBorder(context, x, y, w - 1, h - 1, PLCCols.TRANSPARENT.col, -1, true);
//
                GUIUtil.drawVerticalLine1(context, x + w - 1, y + 1, y + h, PLCCols.TRANSPARENT.col);
            }

            void renderBorder(GuiGraphics context, int x, int y, int dx, int dy, int col, int offset, boolean bottom)
            {
                GUIUtil.drawHorizontalLine1(context, x - offset, x + dx + offset, y - offset, col);
                GUIUtil.drawVerticalLine1(context, x - offset, y - offset, y + dy + offset, col);
                if (bottom)
                    GUIUtil.drawHorizontalLine1(context, x - offset, x + dx + offset, y + dy + offset, col);

                GUIUtil.drawVerticalLine1(context, x + dx + offset, y - offset, y + 2, col);
                GUIUtil.drawVerticalLine1(context, x + dx + offset, y - offset + dy - 2, y + offset + dy, col);
            }
        };

        this.searchWidget.setPos(x, y + gridRows * hSlot + 2);
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        border.render(matrices, mouseX, mouseY, delta, (float) scrollRow / scrollableRows);
        searchWidget.render(matrices, mouseX, mouseY, delta);

        int x, y, i, j;
        for (int m = 0; m < itemsToShow.size(); ++m)
        {
            matrices.pose().pushPose();
            i = m % gridColumns;
            j = m / gridColumns;

            if (j >= gridRows) break;

            x = gridStartX + i * wSlot;
            y = gridStartY + j * hSlot;

            drawSlot(x, y, matrices, getGridItem(i, j));

            matrices.pose().popPose();
        }

        var hoveredItem = getHoveredItem(mouseX, mouseY);
        if (hoveredItem != null)
        {
            matrices.renderTooltip(textRenderer, hoveredItem.stack, mouseX, mouseY);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        scrollRow = (int) Math.min(scrollableRows, Math.max(scrollRow - amount, 0));
        return ClickableWidget.super.mouseScrolled(mouseX, mouseY, amount);
    }

//    @Override
//    public SelectionType getType()
//    {
//        return SelectionType.NONE;
//    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        var hovered = getHoveredItem(mouseX, mouseY);
        if (hovered != null && !hovered.resource().isBlank())
        {
            long amount = Math.min(Screen.hasShiftDown() ? 1 : hovered.amount(), hovered.resource().getItem().getMaxStackSize());
            ResourceAmount<ItemVariant> requested = new ResourceAmount<>(hovered.resource(), amount);
            request.accept(requested);
            updateSearch();
        }

        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        if (searchMode)
        {
            searchWidget.write(chr, modifiers);
            return true;
        }

        if (chr == '/')
        {
            searchMode = true;
            return true;
        }

        return ClickableWidget.super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && searchMode)
        {
            searchMode = false;
            searchWidget.searchString.setLength(0);
            updateSearch();
            return true;
        }
        else
        {
            if (searchMode)
            {
                if (keyCode == GLFW.GLFW_KEY_BACKSPACE)
                {
                    searchWidget.erase(-1);
                }
                return true;
            }
        }
        return false;
    }

    protected Entry getHoveredItem(double mouseX, double mouseY)
    {
        double x = mouseX - gridStartX;
        double y = mouseY - gridStartY;

        if (x < 0 || y < 0)
            return null;

        int i = (int) (x / wSlot);
        int j = (int) (y / hSlot);

        return getGridItem(i, j);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return mouseInGrid(mouseX, mouseY);
    }

    @Override
    public boolean isFocused()
    {
        return true;
    }

    @Override
    public void setFocused(boolean focused)
    {

    }

    protected Entry getGridItem(int i, int j)
    {
        if (!isInGrid(i, j))
            return null;

        int m = scrollRow * gridColumns + i + gridColumns * j;

        if (!(m >= 0 && m < itemsToShow.size()))
            return null;

        return itemsToShow.get(m);
    }


    protected boolean mouseInGrid(double mouseX, double mouseY)
    {
        return mouseX - this.x0() < w && mouseY - this.y0() < h;
    }

    protected boolean isInGrid(int i, int j)
    {
        return i < gridColumns && j < gridRows;
    }

    public void drawSlot(int x, int y, GuiGraphics matrices, Entry entry)
    {
        if (entry == null)
            return;

        matrices.pose().pushPose();
        matrices.pose().translate(x, y, 0);

        matrices.renderItem(entry.stack, 0, 0, 100);
        drawItemInSlot(matrices, textRenderer, entry, 0, 0, null);
        matrices.pose().popPose();
    }

    public void drawItemInSlot(GuiGraphics context, Font textRenderer, Entry entry, int x, int y, @Nullable String countOverride)
    {
        PoseStack matrices = context.pose();

        if (!entry.resource().isBlank())
        {
            matrices.pushPose();
            if (entry.amount() != 1 || !entry.real() || countOverride != null)
            {
                String string = countOverride == null ? String.valueOf(entry.amount()) : countOverride;
                matrices.translate(0.0F, 0.0F, 200.0F);
                matrices.pushPose();
                int maxWidth = textRenderer.width("00");
                int countWidth = textRenderer.width(string);
                float scale = Math.min(1f, (float) maxWidth / countWidth);
                matrices.translate(x + 19 - 2 - textRenderer.width(string) * scale, y + 18 - textRenderer.lineHeight * scale, 0);
                matrices.scale(scale, scale, 1);
                int textCol = entry.real() ? 16777215 : 0xff777777;
                context.drawString(textRenderer, string, 0, 0, textCol, true);
                matrices.popPose();
            }

            if (entry.craftable())
            {
                matrices.pushPose();
                String string = "C";
                float scale = 0.5f;
                matrices.scale(scale, scale, 1);
                context.drawString(textRenderer, string, 0, 0, 16777215, true);
                matrices.popPose();
            }

            if (entry.stack.isBarVisible())
            {
                int i = entry.stack.getBarWidth();
                int j = entry.stack.getBarColor();
                int k = x + 2;
                int l = y + 13;
                context.fill(RenderType.guiOverlay(), k, l, k + 13, l + 2, -16777216);
                context.fill(RenderType.guiOverlay(), k, l, k + i, l + 1, j | 0xFF000000);
            }

            matrices.popPose();
        }
    }

    @Override
    public void setDims(int x, int y, int w, int h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        init();
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

    public class SearchWidget implements Renderable, Rectangle, Point.Mutable
    {
        private final Component searchMessage = NeepMeat.translationKey("screen", "guide.search", "/");

        private final StringBuilder searchString = new StringBuilder();

        private int x, y;

        protected void write(char chr, int modifiers)
        {
            searchString.append(Character.toLowerCase(chr));
            updateSearch();
        }

        protected void erase(int dist)
        {
            if (searchString.isEmpty())
                return;

            searchString.delete(searchString.length() - 1, searchString.length());
            updateSearch();
        }

        public int x0()
        {
            return x;
        }

        public int y0()
        {
            return y;
        }

        public int nw()
        {
            return w;
        }

        public int nh()
        {
            return textRenderer.lineHeight;
        }

        @Override
        public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
        {
            GUIUtil.drawVerticalLine1(matrices, this.x0(), this.y0() - 1, this.y0() + nh() + 3, PLCCols.BORDER.col);
            GUIUtil.drawVerticalLine1(matrices, this.x0() + 1, this.y0() - 1, this.y0() + nh() + 2, PLCCols.TRANSPARENT.col);

            GUIUtil.drawVerticalLine1(matrices, this.x0() + nw() - 1, this.y0() - 1, this.y0() + nh() + 3, PLCCols.BORDER.col);
            GUIUtil.drawVerticalLine1(matrices, this.x0() + nw() - 2, this.y0() - 1, this.y0() + nh() + 2, PLCCols.TRANSPARENT.col);

            GUIUtil.drawHorizontalLine1(matrices, this.x0(), this.x0() + nw() - 1, this.y0() + nh() + 1, PLCCols.TRANSPARENT.col);
            GUIUtil.drawHorizontalLine1(matrices, this.x0(), this.x0() + nw() - 1, this.y0() + nh() + 2, PLCCols.BORDER.col);

            if (!searchMode)
            {
                GUIUtil.drawText(matrices, textRenderer, searchMessage, this.x0() + 2, this.y0() + (nh() - 7) / 2f, PLCCols.TEXT.col, false);
            }
            else
            {
                GUIUtil.drawText(matrices, textRenderer, "/" + searchString, this.x0() + 2, this.y0() + (nh() - 7) / 2f, PLCCols.TEXT.col, false);
            }
        }

        @Override
        public void setPos(int x, int y)
        {
            this.x = x;
            this.y = y;
        }
    }
}
