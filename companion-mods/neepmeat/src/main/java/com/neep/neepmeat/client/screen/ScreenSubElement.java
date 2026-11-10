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

package com.neep.neepmeat.client.screen;

import com.google.common.collect.Lists;
import com.neep.meatlib.client.screen.widget.TooltipRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import java.util.List;

// TODO: Replace with GeneralPanel
public abstract class ScreenSubElement extends AbstractContainerEventHandler implements Renderable, GuiEventListener, TooltipRenderer
{
    protected final List<GuiEventListener> children = Lists.newArrayList();
    protected final List<Renderable> drawables = Lists.newArrayList();
    protected final List<NarratableEntry> selectables = Lists.newArrayList();

    protected int x;
    protected int y;
    protected int screenWidth;
    protected int screenHeight;

    protected final Minecraft client = Minecraft.getInstance();
    protected final Font textRenderer = client.font;

    protected ScreenSubElement()
    {
    }

    public void setDimensions(int screenWidth, int screenHeight)
    {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void init(int screenWidth, int screenHeight)
    {
        clearChildren();

        setDimensions(screenWidth, screenHeight);

        init();
    }

    public void tick()
    {

    }

    protected abstract void init();

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        for(Renderable drawable : this.drawables)
        {
            drawable.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public void renderTooltipLayer(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        for (var child : children)
        {
            if (child instanceof TooltipRenderer toot)
                toot.renderTooltipLayer(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public List<? extends GuiEventListener> children()
    {
        return children;
    }

    protected void clearChildren()
    {
        this.drawables.clear();
        this.children.clear();
        this.selectables.clear();
    }

    public <T extends Renderable & GuiEventListener & NarratableEntry> T addDrawableChild(T t)
    {
        this.drawables.add(t);
        this.children.add(t);
        this.selectables.add(t);
        return t;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return getChildAt(mouseX, mouseY).isPresent();
    }

    public void addDrawable(Renderable t)
    {
        this.drawables.add(t);
    }
}
