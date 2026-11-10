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

package com.neep.meatlib.client.screen.widget;

import com.neep.meatlib.client.screen.auto.CloseListener;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class ParentWidget extends AbstractContainerEventHandler implements ClickableWidget, NarratableEntry, CloseListener, TooltipRenderer
{
    protected final Minecraft client = Minecraft.getInstance();
    protected final Font textRenderer = client.font;
    private final List<ClickableWidget> positionables = new ObjectArrayList<>();
    private final List<Renderable> drawables = new ObjectArrayList<>();
    protected int x, y;
    protected int w, h;

    public ParentWidget(int x, int y, int w, int h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    @Override
    public List<ClickableWidget> children()
    {
        return positionables;
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

    public void init()
    {
        positionables.clear();
        drawables.clear();
    }

    public void tick()
    {

    }

    protected <T extends ClickableWidget> T addChild(T widget)
    {
        positionables.add(widget);
        return widget;
    }

    protected <T extends Renderable> T addChild(T widget)
    {
        drawables.add(widget);
        return widget;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        drawables.forEach(drawable -> drawable.render(context, mouseX, mouseY, delta));
        children().forEach(clickableWidget -> clickableWidget.render(context, mouseX, mouseY, delta));
    }

    @Override
    public void renderTooltipLayer(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        children().forEach(c ->
        {
            if (c instanceof TooltipRenderer tooltipRenderer)
                tooltipRenderer.renderTooltipLayer(context, mouseX, mouseY, delta);
        });
    }

    @Override
    public void setPos(int x, int y)
    {
        this.x = x;
        this.y = y;
        init();
    }

    @Override
    public void updateNarration(NarrationElementOutput builder)
    {

    }

    @Override
    public NarrationPriority narrationPriority()
    {
//        return SelectionType.NONE; // Not sure what this should really return
        if (this.isFocused())
        {
            return NarratableEntry.NarrationPriority.FOCUSED;
        }
        else
        {
//            return this.hovered ? Selectable.SelectionType.HOVERED : Selectable.SelectionType.NONE;
            return NarrationPriority.NONE;
        }
    }

    @Override
    public void close()
    {
        for (var element : children())
        {
            if (element instanceof CloseListener closeListener)
            {
                closeListener.close();
            }
        }
    }
}
