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

import com.neep.meatlib.client.screen.auto.Panel;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.client.screen.util.BorderScrollRight;
import com.neep.neepmeat.item.filter.Filter;
import com.neep.neepmeat.item.filter.FilterList;
import com.neep.neepmeat.item.filter.ItemFilter;
import com.neep.neepmeat.item.filter.TagFilter;
import com.neep.meatlib.mixin.client.AbstractParentElementAccessor;
import com.neep.neepmeat.transport.screen.FilterWidgetHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class FilterPanel extends Panel
{
    private final FilterWidgetHandler handler;
    private final Supplier<ItemStack> cursorStack;

    private final List<FilterEntryWidget<?>> entries = new ArrayList<>();

    private float scroll;
    private BorderScrollRight entriesBorder;

//    private final List<Element> drawables = new ObjectArrayList<>();

    public FilterPanel(FilterWidgetHandler handler, Supplier<ItemStack> cursorStack)
    {
        this.handler = handler;
        this.cursorStack = cursorStack;

        handler.updateToClient.receiver(this::updateOnClient);
    }

    @Override
    public void init()
    {
//        drawables.clear();

//        Background background = new Background(x, y, backgroundWidth, backgroundHeight, 6, () -> PLCCols.BORDER.col);
//        Border background = addDrawable(new Border(x, y, backgroundWidth, backgroundHeight, 6, () -> PLCCols.BORDER.col));
//        Rectangle withoutPadding = background.withoutPadding();

//        int entriesX = withoutPadding.x();
//        int entriesWidth = withoutPadding.w();
//        int entriesHeight = 150;

//        addChild(background);


        Rectangle entriesBounds = new ImmutableRect(x0(), y0(), nw(), nh());
        entriesBorder = new BorderScrollRight(entriesBounds, 0, () -> PLCCols.BORDER.col);

//        var inventoryBorder = new PlayerSlotsBorder(entriesX, entriesBounds.y() + entriesBounds.h() + 1, () -> PLCCols.BORDER.col);
//        drawables.add(inventoryBorder);

        createEntries();
    }

    private void createEntries()
    {
        FilterList filters = handler.getFilters();
        int yOff = (int) (entriesBorder.y0() + scroll) + 2;
        int xOff = entriesBorder.x0() + 2;

        // In order make the state of each EntryWidget persistent across syncs, some jank is necessary.
        for (int i = 0; i < filters.size(); i++)
        {
            FilterList.Entry entry = filters.getEntries().get(i);
            Filter filter = entry.getFilter();
            FilterEntryWidget<?> widget;
            if (i < entries.size() && entries.get(i).filter.getType() != filter.getType())
            {
                // Change type
                widget = createWidget(i, entry);
                entries.set(i, widget);
                widget.init();
            }
            else if (i == entries.size())
            {
                widget = createWidget(i, entry);
                entries.add(i, widget);
                widget.init();
            }
            else
            {
                widget = entries.get(i);
                widget.updateFilter(entry);
            }

            widget.setPos(xOff, yOff);
            yOff += widget.nh() - 1;
        }

        // Remove excess entries
        entries.removeIf(entry -> entry.index >= filters.size());
    }

    // Jank!
    private FilterEntryWidget<?> createWidget(int index, FilterList.Entry entry)
    {
        int w = entriesBorder.nw() - 4;
        if (entry.getFilter() instanceof ItemFilter itemFilter)
        {
            return new ItemFilterWidget(w, index, entry, itemFilter, handler, cursorStack);
        }
        else if (entry.getFilter() instanceof TagFilter tagFilter)
        {
            return new TagFilterWidget(w, index, entry, tagFilter, handler);
        }

        return new EmptyFilterWidget(w, index, handler);
    }

    @Override
    protected List<Renderable> drawables()
    {
        return List.of();
    }

    @Override
    public List<GuiEventListener> children()
    {
        return new ObjectArrayList<>(entries);
    }

    private float maxScroll()
    {
        return -100;
    }

    public void updateOnClient(FilterList filter)
    {
        handler.updateOnClient(filter);
        createEntries();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);

        entriesBorder.render(context, mouseX, mouseY, delta, scroll / maxScroll());

        context.enableScissor(entriesBorder.x0(), entriesBorder.y0() + 2, entriesBorder.x0() + entriesBorder.nw(), entriesBorder.y0() + entriesBorder.nh() - 2);
        for (var entry : entries)
        {
            entry.render(context, mouseX, mouseY, delta);
        }
        context.disableScissor();
    }

//    public <T extends Drawable & Point.Mutable & Rectangle & Element> VStackPanel addChild(T widget)
//    {
//        children.add(widget);
//        drawables.add(widget);
//        elements.add(widget);
//        return this;
//    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        for (var entry : entries)
        {
            if (entry.mouseClicked(mouseX, mouseY, button))
            {
                setFocused(entry);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_DELETE)
        {
            for (var entry : entries)
            {
                if (entry.isFocused())
                {
                    handler.removeFilter.emitter().apply(entry.index);
                }
            }
        }

        for (var entry : entries)
        {
            if (entry.keyPressed(keyCode, scanCode, modifiers))
                return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        for (var entry : entries)
        {
            if (entry.charTyped(chr, modifiers))
                return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        scroll = (float) Mth.clamp(scroll + amount * 2, maxScroll(), 0);

        int yOff = (int) (entriesBorder.y0() + scroll) + 2;
        int xOff = entriesBorder.x0() + 2;
        for (var entry : entries)
        {
            entry.setPos(xOff, yOff);
            yOff += entry.nh() - 1;
        }

        return true;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused)
    {
        AbstractParentElementAccessor accessor = (AbstractParentElementAccessor) this;
        if (accessor.getFieldFocused() != null && accessor.getFieldFocused() != focused)
            accessor.getFieldFocused().setFocused(false);

        if (focused != null)
        {
            focused.setFocused(true);
        }

        accessor.setFieldFocused(focused);
    }
}
