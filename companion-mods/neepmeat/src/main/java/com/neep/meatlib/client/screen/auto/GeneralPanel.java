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

import com.neep.meatlib.client.screen.primitive.Point;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;

public abstract class GeneralPanel extends Panel
{
    protected final List<Point.Mutable> children = new ObjectArrayList<>();
    private final List<Renderable> drawables = new ObjectArrayList<>();
    private final List<GuiEventListener> elements = new ObjectArrayList<>();
    protected final Font textRenderer = Minecraft.getInstance().font;

    @Override
    public void init()
    {

    }

    @Override
    protected List<Renderable> drawables()
    {
        return drawables;
    }

    @Override
    public List<GuiEventListener> children()
    {
        return elements;
    }

    public void clearWidgets()
    {
        children.clear();
        drawables.clear();
        elements.clear();
    }

    /** Yarn name for {@link #clearWidgets()}. */
    public void clearChildren()
    {
        clearWidgets();
    }

    public <T extends Renderable & Point.Mutable & Rectangle & GuiEventListener> T addChild(T widget)
    {
        children.add(widget);
        drawables.add(widget);
        elements.add(widget);
        return widget;
    }

    public <T extends Renderable> T addRenderableOnly(T widget)
    {
        drawables.add(widget);
        return widget;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double amount)
    {
        return super.mouseScrolled(mouseX, mouseY, scrollX, amount);
    }
}
