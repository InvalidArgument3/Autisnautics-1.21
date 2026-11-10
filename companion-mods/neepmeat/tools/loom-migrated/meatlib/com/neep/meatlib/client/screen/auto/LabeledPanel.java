/*
 * Copyright (c) MeatWheeze 2026.
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
import com.neep.meatlib.client.screen.widget.Label;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

public class LabeledPanel extends Panel
{
    private final ClickableWidget widget;

    public final Label label;

    public LabeledPanel(Component label, ClickableWidget widget)
    {
        this.label = new Label(label);
        this.widget = widget;
    }

    public LabeledPanel(Supplier<Component> label, ClickableWidget widget)
    {
        this.label = new Label(label);
        this.widget = widget;
    }

    public LabeledPanel col(int col)
    {
        label.col(col);
        return this;
    }

    public LabeledPanel shadow(boolean shadow)
    {
        label.shadow(shadow);
        return this;
    }

    public LabeledPanel centre(boolean centre)
    {
        label.centre(centre);
        return this;
    }

    @Override
    public int nw()
    {
        return widget.nw();
    }

    @Override
    public int nh()
    {
        return widget.nh() + label.textRenderer.lineHeight + 2;
    }

    @Override
    public void init()
    {
        int textHeight = label.textRenderer.lineHeight + 2;
        setDims(label, x, y, w, textHeight);
        setDims(widget, x, y + textHeight, w, h - textHeight);
    }

    @Override
    protected List<Renderable> drawables()
    {
        return List.of(label, widget);
    }

    @Override
    public List<GuiEventListener> children()
    {
        return List.of(label, widget);
    }
}
