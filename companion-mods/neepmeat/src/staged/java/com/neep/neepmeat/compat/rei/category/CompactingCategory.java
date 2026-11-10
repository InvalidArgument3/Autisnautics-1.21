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
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepmeat.compat.rei.NMREIPlugin;
import com.neep.neepmeat.compat.rei.display.CompactingDisplay;
import com.neep.neepmeat.init.NMBlocks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CompactingCategory implements DisplayCategory<CompactingDisplay>
{
    @Override
    public CategoryIdentifier<? extends CompactingDisplay> getCategoryIdentifier()
    {
        return NMREIPlugin.COMPACTING;
    }

    @Override
    public Renderer getIcon()
    {
        return EntryStacks.of(NMBlocks.CHARNEL_COMPACTOR);
    }

    @Override
    public Component getTitle()
    {
        return Component.translatable("category." + NeepMeat.NAMESPACE + ".compacting");
    }

    @Override
    public DisplayRenderer getDisplayRenderer(CompactingDisplay display)
    {
        return new DisplayRenderer()
        {
            private final Component text = Component.translatable("text." + NeepMeat.NAMESPACE + ".compacting.page", display.getPage() + 1);

            @Override
            public int getHeight()
            {
                return 10 + Minecraft.getInstance().font.lineHeight;
            }

            @Override
            public void render(GuiGraphics matrices, Rectangle rectangle, int mouseX, int mouseY, float delta)
            {
                Font textRenderer = Minecraft.getInstance().font;
                GUIUtil.drawText(matrices, textRenderer, text, rectangle.x + 5, rectangle.y + 6, -1, false);
            }
        };
    }

    @Override
    public List<Widget> setupDisplay(CompactingDisplay display, Rectangle bounds)
    {
        List<Widget> widgets = Lists.newArrayList();
        Point startingPoint = new Point(bounds.x + bounds.width - 55, bounds.y + 110);
        List<EntryIngredient> stacks = new ArrayList<>(display.getInputEntries());
        widgets.add(Widgets.createRecipeBase(bounds));
        int i = 0;
        for (int y = 0; y < 5; y++)
            for (int x = 0; x < 7; x++)
            {
                EntryIngredient entryStack = stacks.size() > i ? stacks.get(i) : EntryIngredient.empty();
//                if (!entryStack.isEmpty()) {
//                    ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.object2FloatEntrySet().stream().filter(entry -> entry.getKey() != null && Objects.equals(entry.getKey().asItem(), entryStack.get(0).getValue())).findAny().map(Map.Entry::getValue).ifPresent(chance -> {
//                        for (EntryStack<?> stack : entryStack) {
//                            stack.tooltip(Text.translatable("text.rei.composting.chance", MathHelper.fastFloor(chance * 100)).formatted(Formatting.YELLOW));
//                        }
//                    });
//                }
                widgets.add(Widgets.createSlot(new Point(bounds.getCenterX() - 72 + 9 + x * 18, bounds.y + 12 + y * 18)).entries(entryStack).markInput());
                i++;
            }
        widgets.add(Widgets.createArrow(new Point(startingPoint.x - 1 - 5, startingPoint.y + 7 - 5)));
        widgets.add(Widgets.createResultSlotBackground(new Point(startingPoint.x + 33 - 5, startingPoint.y + 8 - 5)));
        widgets.add(Widgets.createSlot(new Point(startingPoint.x + 33 - 5, startingPoint.y + 8 - 5)).entries(display.getOutputEntries().get(0)).disableBackground().markOutput());
        return widgets;
    }

    @Override
    public int getDisplayHeight()
    {
        return 140;
    }

    @Override
    public int getFixedDisplaysPerPage()
    {
        return 1;
    }
}