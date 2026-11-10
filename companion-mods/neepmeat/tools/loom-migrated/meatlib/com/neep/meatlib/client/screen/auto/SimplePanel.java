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

package com.neep.meatlib.client.screen.auto;

import com.neep.meatlib.client.screen.widget.ClickableWidget;
import java.util.List;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;

public abstract class SimplePanel extends Panel
{
    protected ClickableWidget element = EndpointPanel.INSTANCE;

    protected int padding = 0;

    protected boolean inheritWidth = false;
    protected boolean inheritHeight = false;

    public SimplePanel()
    {
    }

    public SimplePanel solidW()
    {
        inheritWidth = true;
        w = element.nw() + 2 * padding;
        return this;
    }

    public SimplePanel solidH()
    {
        inheritHeight = true;
        h = element.nh() + 2 * padding;
        return this;
    }

    protected void setDims()
    {
        setDims(element, x + padding, y + padding,
                inheritWidth ? element.nw() : w - 2 * padding,
                inheritHeight ? element.nh() : h - 2 * padding);
    }

    @Override
    public final void init()
    {
        element = initMainElement();
    }

    protected abstract ClickableWidget initMainElement();

    @Override
    protected List<Renderable> drawables()
    {
        return List.of(element);
    }

    @Override
    public List<GuiEventListener> children()
    {
        return List.of(element);
    }
}
