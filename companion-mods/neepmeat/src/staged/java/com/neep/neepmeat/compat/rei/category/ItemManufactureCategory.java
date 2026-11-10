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
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.compat.rei.NMREIPlugin;
import com.neep.neepmeat.compat.rei.display.ItemManufactureDisplay;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import java.util.List;

public class ItemManufactureCategory extends ManufactureCategory<ItemManufactureDisplay>
{
    @Override
    public CategoryIdentifier<? extends ItemManufactureDisplay> getCategoryIdentifier()
    {
        return NMREIPlugin.ITEM_MANUFACTURE;
    }

    @Override
    public List<Widget> setupDisplay(ItemManufactureDisplay display, Rectangle bounds)
    {
        Point startPoint = new Point(bounds.x + 5, bounds.y + 5);
        List<Widget> widgets = Lists.newArrayList();
//        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(new ManufactureCategory.OutlineWidget(bounds));

        var base = new LabelledSlot(startPoint, Component.nullToEmpty("Base: "), EntryStacks.of(display.getBase()));
        widgets.add(base);

        var output = new LabelledSlot(new Point(startPoint.x + 20 + base.width(), startPoint.y), Component.nullToEmpty("Output: "), display.getOutputEntries().get(0).get(0));
        widgets.add(output);

        int entryY = startPoint.y + 22;
        int entryX = startPoint.x + 1;
        for (var step : display.getSteps())
        {
            ManufactureCategory.EntryWidget widget = new ManufactureCategory.EntryWidget(new Point(entryX, entryY), step, 160 - 20);
            widgets.add(widget);
            entryY += widget.height() + 2;
        }

        return widgets;
    }

    @Override
    public Component getTitle()
    {
        return Component.translatable("category.neepmeat.manufacture");
    }

    public static int borderCol()
    {
        return PLCCols.BORDER.col;
    }
}
