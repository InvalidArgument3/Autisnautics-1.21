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

package com.neep.neepmeat.transport.client.screen.filter;

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.auto.GeneralPanel;
import com.neep.meatlib.client.screen.primitive.Point;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.widget.AbstractClickableWidget;
import com.neep.meatlib.client.screen.widget.CheckboxWidget;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.item.filter.Filter;
import com.neep.neepmeat.item.filter.FilterList;
import com.neep.neepmeat.transport.screen.FilterWidgetHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public abstract class FilterEntryWidget<T extends Filter> extends GeneralPanel implements Point.Mutable, Rectangle
{
    protected final List<ClickableWidget> positionables = new ArrayList<>();
    protected boolean focused;

    private final Component name;
    protected FilterList.Entry entry;
    protected T filter;

    protected final int index;
    protected final FilterWidgetHandler handler;

    public FilterEntryWidget(int w, int h, int index, Component name, FilterList.Entry entry, T filter, FilterWidgetHandler handler)
    {
        this.w = w;
        this.h = h;
        this.index = index;
        this.name = name;
        this.entry = entry;
        this.filter = filter;
        this.handler = handler;
    }

    public void init()
    {
        super.init();
        clearChildren();
        positionables.clear();

        addChild(new InvertButtonWidget(
                x1() - 18 - 2, this.y0() + 3,
                11, 11,
                () -> entry.getInverted(),
                Component.nullToEmpty("Invert"),
                (button, toggled) -> handler.setInverted(index, toggled)));

        addChild(new RemoveWidget())
                .setPos(x1() - 8, this.y0() + 4);
    }

    @Override
    public void setPos(int x, int y)
    {
        super.setPos(x, y);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (isMouseOver(mouseX, mouseY))
        {
            super.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return isWithin(mouseX, mouseY) || super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public <R extends Renderable & Point.Mutable & Rectangle & GuiEventListener> R addChild(R widget)
    {
        super.addChild(widget);
        if (widget instanceof ClickableWidget clickableWidget)
        {
            positionables.add(clickableWidget);
        }
        return widget;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        int col = isFocused() ? PLCCols.SELECTED.col : PLCCols.BORDER.col;
        GUIUtil.renderBorderInner(context, x, y, w, h, col, 0);

        GUIUtil.drawText(context, textRenderer, name, this.x0() + 3, this.y0() + 3, PLCCols.TEXT.col, false);

        super.render(context, mouseX, mouseY, delta);
    }

    protected void updateToServer()
    {
        handler.updateToServer.emitter().apply(index, filter.writeNbt(new CompoundTag()));
    }

    public void updateFilter(FilterList.Entry entry)
    {
        // EEEEEK
        this.entry = entry;
        this.filter = (T) entry.getFilter();
    }

    protected T getFilter()
    {
        return filter;
    }

    @Override
    public void setFocused(boolean focused)
    {
        this.focused = focused;
        super.setFocused(focused);
        if (!focused)
        {
            setFocused(null);
//            children.forEach(c -> c.setFocused(false));
        }
    }

    @Override
    public boolean isFocused()
    {
        return focused || super.isFocused();
    }

    public static class InvertButtonWidget extends CheckboxWidget
    {
        public InvertButtonWidget(int x, int y, int w, int h, BooleanSupplier toggled, Component message, ToggleAction onToggle)
        {
            super(x, y, w, h, toggled, message, onToggle);
        }

        @Override
        protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            String box = isToggled() ? "☒" : "☐";
            int borderCol = borderActive(mouseX, mouseY) ? PLCCols.SELECTED.col : PLCCols.BORDER.col;

            int boxWidth = textRenderer.width(box);
            int boxHeight = textRenderer.lineHeight;

            var label = getMessage();
            int labelWidth = textRenderer.width(label);

            int textX = x0() + (nh() - boxWidth) / 2;
            int textY = this.y0() + (nh() - boxHeight) / 2;

            GUIUtil.drawText(context, textRenderer, Component.nullToEmpty(box), textX, textY, borderCol, false);
            GUIUtil.drawText(context, textRenderer, label, textX - labelWidth - 3, textY, PLCCols.TEXT.col, false);
        }
    }

    public class RemoveWidget extends AbstractClickableWidget
    {
//        private static final String TEXT = "×";
        public RemoveWidget()
        {
            width = textRenderer.width("×");
            height = textRenderer.lineHeight;
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            super.onClick(mouseX, mouseY);
            handler.removeFilter.emitter().apply(index);
        }

        @Override
        protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            int borderCol = isWithin(mouseX, mouseY) ? PLCCols.SELECTED.col : PLCCols.BORDER.col;
            GUIUtil.drawText(context, textRenderer, "×", x0(), this.y0(), borderCol, false);
        }
    }
}
