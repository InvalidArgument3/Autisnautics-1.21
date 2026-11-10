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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;

public class GridPanel extends Panel
{
    private final List<ClickableWidget> children;
    private final int rows;
    private final int cols;

    public GridPanel(int rows, int cols)
    {
        this.rows = rows;
        this.cols = cols;

        var array = new ClickableWidget[rows * cols];
        Arrays.fill(array, EndpointPanel.INSTANCE);
        children = new ObjectArrayList<>(array);
    }

    @Override
    public void init()
    {
        int elW = Math.floorDiv(nw(), cols);
        int elH = Math.floorDiv(nh(), rows);

        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                var element = children.get(i * cols + j);
                if (element != EndpointPanel.INSTANCE)
                {
                    setDims(element, x0() + j * elW, this.y0() + i * elH, elW, elH);
                }
            }
        }
    }

    @Override
    protected List<Renderable> drawables()
    {
        // I caare no moree
        return (List<Renderable>) (List) children;
    }

    @Override
    public List<GuiEventListener> children()
    {
        return (List<GuiEventListener>) (List) children;
    }

    public GridPanel set(int i, int j, ClickableWidget widget)
    {
        children.set(j * cols + i, widget);
        return this;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}
