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

import com.neep.meatlib.client.screen.auto.VStackPanel;
import com.neep.neepmeat.client.screen.plc.PLCScreenButton;
import com.neep.neepmeat.item.filter.Filter;
import com.neep.neepmeat.item.filter.Filters;
import com.neep.neepmeat.transport.screen.FilterWidgetHandler;
import net.minecraft.network.chat.Component;

public class FilterButtonsPanel extends VStackPanel
{
    private final FilterWidgetHandler handler;

    public FilterButtonsPanel(FilterWidgetHandler handler, int padding, int elementPadding)
    {
        super(padding, elementPadding, false);
        this.handler = handler;

        addChild(new AddFilterButton(0, 0, 128, Filters.ITEM, Component.empty()));
        addChild(new AddFilterButton(0, 0, 128 + 16, Filters.TAG, Component.empty()));

        w = 18;
        h = 18;
    }

    @Override
    public void init()
    {
        super.init();
    }

    private class AddFilterButton extends PLCScreenButton
    {
        private final int u;
        private final Filter.Constructor<?> filter;
        private final Component tooltip;

        public AddFilterButton(int x, int y, int u, Filter.Constructor<?> filter, Component tooltip)
        {
            super(x, y, Component.empty());
            this.u = u;
            this.filter = filter;
            this.tooltip = tooltip;
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            super.onClick(mouseX, mouseY);
            handler.addFilter(filter);
        }

        @Override
        protected int getU()
        {
            return u;
        }
    }
}
