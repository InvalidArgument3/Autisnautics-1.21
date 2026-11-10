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
import com.neep.meatlib.recipe.ingredient.entity.EntityRecipeInput;
import com.neep.meatlib.theme.PLCCols;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepmeat.compat.rei.NMREIPlugin;
import com.neep.neepmeat.compat.rei.display.EntityToItemDisplay;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import java.util.List;

public class EntityToItemManufactureCategory extends ManufactureCategory<EntityToItemDisplay>
{
    @Override
    public CategoryIdentifier<? extends EntityToItemDisplay> getCategoryIdentifier()
    {
        return NMREIPlugin.ENTITY_TO_ITEM;
    }

    @Override
    public List<Widget> setupDisplay(EntityToItemDisplay display, Rectangle bounds)
    {
        Point startPoint = new Point(bounds.x + 5, bounds.y + 5);
        List<Widget> widgets = Lists.newArrayList();

        widgets.add(new OutlineWidget(bounds));

        var base = new LabelledEntitySlot(startPoint, Component.nullToEmpty("Base: "), display.getBase());
        widgets.add(base);

        var output = new LabelledSlot(new Point(startPoint.x + 20 + base.width(), startPoint.y + 18), Component.nullToEmpty("Output: "), display.getOutputEntries().get(0).get(0));
        widgets.add(output);

        int entryY = startPoint.y + 22 + 18;
        int entryX = startPoint.x + 1;

        for (var step : display.getSteps())
        {
            EntryWidget widget = new EntryWidget(new Point(entryX, entryY), step, 160 - 20);
            widgets.add(widget);
            entryY += widget.height() + 2;
        }

        return widgets;
    }


    @Override
    public Component getTitle()
    {
        return Component.translatable("category.neepmeat.entity_to_item_manufacture");
    }

    public static int borderCol()
    {
        return PLCCols.BORDER.col;
    }

    static class LabelledEntitySlot extends Widget
    {
        private final Point origin;
        private final EntityRecipeInput entityType;
        private final Component name;
        private final Point slotOrigin;
        private final Font textRenderer = Minecraft.getInstance().font;

        LabelledEntitySlot(Point origin, Component name, EntityRecipeInput entityType)
        {
            this.name = name;
            this.origin = origin;
            this.entityType = entityType;
            this.slotOrigin = new Point(origin.x + textRenderer.width(name) + 2, origin.y);
        }

        public int width()
        {
            return textRenderer.width(name) + 2 + 20;
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            GUIUtil.drawText(context, textRenderer, name, origin.x, origin.y, borderCol(), true);

            Component entityName = entityType.format();
            GUIUtil.drawText(context, textRenderer, entityName, slotOrigin.x, slotOrigin.y, borderCol(), true);

//            GUIUtil.renderBorder(context, slotOrigin.x - 1, slotOrigin.y - 1, 17, 17, borderCol(), 0);
//            GUIUtil.renderBorder(context, slotOrigin.x, slotOrigin.y, 15, 15, PLCCols.TRANSPARENT.col, 0);
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return List.of();
        }
    }
}
