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

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarratableEntry;

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.client.screen.primitive.Point;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.widget.TooltipRenderer;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class Panel extends AbstractContainerEventHandler implements ContainerEventHandler, ClickableWidget, Rectangle.Mutable, NarratableEntry, CloseListener, TooltipRenderer, ScreenInitialisable
{
    protected boolean focused;
    protected boolean dragging;
    protected int x, y;
    protected int w, h;

    public Panel()
    {

    }

    public static boolean traverse(GuiEventListener panel, double mx, double my, Object2BooleanFunction<GuiEventListener> function)
    {
        if (function.apply(panel))
            return true;

        if (panel instanceof ContainerEventHandler parentElement)
        {
            for (var panel1 : parentElement.children())
            {
                if (panel1.isMouseOver(mx, my))
                {
                    if (traverse(panel1, mx, my, function))
                        return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        if (button == 0)
        {
            return this.getChildAt(mouseX, mouseY).filter(element -> element.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)).isPresent();
        }

        return false;
    }

    @Override
    public abstract void init();

    protected abstract List<Renderable> drawables();

    public abstract List<GuiEventListener> children();

//    @Override
//    public boolean mouseClicked(double mouseX, double mouseY, int button)
//    {
//        for (var child : children())
//            if (child.mouseClicked(mouseX, mouseY, button))
//                return true;
//
//        return false;
//    }
//
//    @Override
//    public boolean mouseReleased(double mouseX, double mouseY, int button)
//    {
//        return ClickableWidget.super.mouseReleased(mouseX, mouseY, button);
//        for (var child : children())
//            if (child.mouseClicked(mouseX, mouseY, button))
//                return true;
//
//        return false;
//    }


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

    @Override
    public void setDims(int x, int y, int w, int h)
    {
        if (this.x != x || this.y != y || this.w != w || this.h != h)
        {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            init();
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        drawables().forEach(drawable -> drawable.render(context, mouseX, mouseY, delta));

    }

    protected void debugOutline(GuiGraphics context)
    {
        GUIUtil.renderBorderInner(context, x, y, w, h, 0xFF0000FF, 0);
    }

    @Override
    public void renderTooltipLayer(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        drawables().forEach(drawable ->
        {
            if (drawable instanceof TooltipRenderer tooltipRenderer)
                tooltipRenderer.renderTooltipLayer(context, mouseX, mouseY, delta);
        });
    }

    @Override
    public void setFocused(boolean focused)
    {
        this.focused = focused;
    }

    @Override
    public boolean isFocused()
    {
        return focused;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority()
    {
        return focused ? NarratableEntry.NarrationPriority.FOCUSED : NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput builder)
    {

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

    protected static void setDims(GuiEventListener element, int x, int y, int w, int h)
    {
        if (element instanceof Rectangle.Mutable mutable)
            mutable.setDims(x, y, w, h);
        else if (element instanceof Point.Mutable mutable)
            mutable.setPos(x, y);
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused)
    {
        GuiEventListener prevFocused = getFocused();
        if (prevFocused != null && prevFocused != focused)
            prevFocused.setFocused(false);
        if (focused != null)
            focused.setFocused(true);
        super.setFocused(focused);
    }
}
