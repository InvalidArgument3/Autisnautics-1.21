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
import com.neep.neepmeat.compat.rei.display.TrommelDisplay;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.machine.small_trommel.SmallTrommelBlockEntity;
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

public class TrommelCategory implements DisplayCategory<TrommelDisplay>
{
    @Override
    public CategoryIdentifier<? extends TrommelDisplay> getCategoryIdentifier()
    {
        return NMREIPlugin.TROMMEL;
    }

    @Override
    public Component getTitle()
    {
        return Component.translatable("category." + NeepMeat.NAMESPACE + ".trommel");
    }

    @Override
    public Renderer getIcon()
    {
        return EntryStacks.of(NMBlocks.SMALL_TROMMEL);
    }

    @Override
    public List<Widget> setupDisplay(TrommelDisplay display, Rectangle bounds)
    {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.y + 10);

        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));

        widgets.add(Widgets.createLabel(new Point(bounds.x + bounds.width - 5, bounds.y + 5),
                NeepMeat.translationKey("category", "general.energy", PowerUtil.energyForRecipe(SmallTrommelBlockEntity.PU_PROCESS_ENERGY))).noShadow().rightAligned().color(0xFF404040, 0xFFBBBBBB));

        widgets.add(Widgets.createArrow(new Point(startPoint.x + 24, startPoint.y + 8)));

        // Input slot
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 1, startPoint.y + 9)).entries(display.getInputEntries().get(0)).markInput());

        // Output slot
        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 61, startPoint.y + 9)));
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 61, startPoint.y + 9)).entries(display.getOutputEntries().get(0)).disableBackground().markOutput());

        // Auxiliary output slot
        if (display.getOutputEntries().size() > 1)
        {
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 87, startPoint.y + 9)).entries(display.getOutputEntries().get(1)).markOutput());
        }
        return widgets;
    }

    @Override
    public int getDisplayHeight()
    {
        return 55;
    }
}
