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
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.compat.rei.NMREIPlugin;
import com.neep.neepmeat.compat.rei.display.GrindingDisplay;
import com.neep.neepmeat.init.NMBlocks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import java.util.List;

public class GrindingCategory implements DisplayCategory<GrindingDisplay>
{
    @Override
    public CategoryIdentifier<? extends GrindingDisplay> getCategoryIdentifier()
    {
        return NMREIPlugin.GRINDING;
    }

    @Override
    public Component getTitle()
    {
        return Component.translatable("category." + NeepMeat.NAMESPACE + ".grinding");
    }

    @Override
    public Renderer getIcon()
    {
        return EntryStacks.of(NMBlocks.CRUSHER);
    }

    @Override
    public List<Widget> setupDisplay(GrindingDisplay display, Rectangle bounds)
    {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.y + 10);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));

        widgets.add(Widgets.createLabel(new Point(bounds.x + bounds.width - 5, bounds.y + 5),
                NeepMeat.translationKey("category", "general.energy", PowerUtil.energyForRecipe(display.processTime()))).noShadow().rightAligned().color(0xFF404040, 0xFFBBBBBB));

        widgets.add(Widgets.createArrow(new Point(startPoint.x + 25, startPoint.y + 9)));
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 1, startPoint.y + 9)).entries(display.getInputEntries().get(0)).markInput());
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 61, startPoint.y + 9)).entries(display.getOutputEntries().get(0)).markOutput());
        if (display.getOutputEntries().size() > 1)
        {
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 81, startPoint.y + 9)).entries(display.getOutputEntries().get(1)).markOutput());
        }
        return widgets;
    }

    @Override
    public int getDisplayHeight()
    {
        return 50;
    }
}
