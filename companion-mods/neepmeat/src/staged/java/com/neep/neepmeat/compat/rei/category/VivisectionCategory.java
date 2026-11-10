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

package com.neep.neepmeat.compat.rei.category;

import com.google.common.collect.Lists;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.compat.rei.NMREIPlugin;
import com.neep.neepmeat.compat.rei.display.VivisectionDisplay;
import com.neep.neepmeat.init.NMItems;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class VivisectionCategory implements DisplayCategory<VivisectionDisplay>
{
    @Override
    public CategoryIdentifier<? extends VivisectionDisplay> getCategoryIdentifier()
    {
        return NMREIPlugin.VIVISECTION;
    }
    
    @Override
    public Renderer getIcon()
    {
        return EntryStacks.of(NMItems.TISSUE_OPERATOR);
    }
    
    @Override
    public Component getTitle()
    {
        return Component.translatable("category." + NeepMeat.NAMESPACE + ".vivisection");
    }

    @Override
    public List<Widget> setupDisplay(VivisectionDisplay display, Rectangle bounds)
    {
        Point startPoint = new Point(bounds.getCenterX() - 51, bounds.y + 10);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));

        if (!display.getEntities().isEmpty())
        {
            widgets.add(Widgets.createLabel(new Point(startPoint.x + 5, bounds.y + 5), display.getEntities().get(0)));
        }
        else if (!display.getInputEntries().isEmpty())
        {
            widgets.add(Widgets.createSlot(new Point(startPoint.x, startPoint.y + 9)).markInput().backgroundEnabled(true)
                    .entries(display.getInputEntries().get(0)));
        }

        widgets.add(Widgets.createArrow(new Point(startPoint.x + 25, startPoint.y + 9)));

        var output = display.getOutputEntries();
        int ny = Mth.ceil(output.size() / 3f);

        for (int j = 0; j < ny; ++j)
        {
            for (int i = 0; i < 3; ++i)
            {
                int idx = j * 3 + i;
                if (idx < output.size())
                {
                    widgets.add(Widgets.createSlot(
                            new Point(startPoint.x + 61 + i * 18, startPoint.y + 9 + j * 18)).entries(output.get(idx)));
                }
                else
                {
                    widgets.add(Widgets.createSlot(
                            new Point(startPoint.x + 61 + i * 18, startPoint.y + 9 + j * 18)));
                }
            }
        }
        return widgets;
    }

    @Override
    public int getDisplayHeight()
    {
        return 55;
    }

    @Override
    public int getDisplayWidth(VivisectionDisplay display)
    {
        return 150;
    }
}