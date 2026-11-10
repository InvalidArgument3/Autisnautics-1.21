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
import com.neep.meatlib.theme.PLCCols;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepmeat.compat.rei.NMREIPlugin;
import com.neep.neepmeat.compat.rei.display.TransformingToolDisplay;
import com.neep.neepmeat.plc.PLCBlocks;
import com.neep.neepmeat.plc.recipe.CombineStep;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import com.neep.meatlib.transfer.ItemVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public class TransformingToolCategory implements DisplayCategory<TransformingToolDisplay>
{
    private static final ResourceLocation GHOST_AXE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/ghost_axe.png");
    private static final ResourceLocation GHOST_SWORD = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/ghost_sword.png");

    @Override
    public CategoryIdentifier<TransformingToolDisplay> getCategoryIdentifier()
    {
        return NMREIPlugin.TRANSFORMING_TOOL;
    }

    @Override
    public Component getTitle()
    {
        return Component.translatable("category." + NeepMeat.NAMESPACE + ".transforming_tool");
    }

    @Override
    public Renderer getIcon()
    {
        return EntryStacks.of(PLCBlocks.PLC);
    }

    @Override
    public List<Widget> setupDisplay(TransformingToolDisplay display, Rectangle bounds)
    {
        Point startPoint = new Point(bounds.x + 5, bounds.y + 5);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(new ManufactureCategory.OutlineWidget(bounds));

        var base = new ItemManufactureCategory.LabelledSlot(startPoint, Component.nullToEmpty("Base: "), EntryStacks.of(display.getBase()));
        widgets.add(base);

//        var output = new ItemManufactureCategory.LabelledSlot(new Point(startPoint.x + 20 + base.width(), startPoint.y), Text.of("Output: "), );
//        widgets.add(output);

        int entryY = startPoint.y + 22;
        int entryX = startPoint.x + 1;

        ToolWidget toolWidget;
        toolWidget = new ToolWidget(new Point(entryX, entryY), 160 - 20, GHOST_AXE);
        widgets.add(toolWidget);
        entryY += toolWidget.height() + 2;
        toolWidget = new ToolWidget(new Point(entryX, entryY), 160 - 20, GHOST_SWORD);
        widgets.add(toolWidget);
        entryY += toolWidget.height() + 2;
        ManufactureCategory.EntryWidget entryWidget = new ManufactureCategory.EntryWidget(new Point(entryX, entryY),
                display.getStep(), 120 - 20);
        widgets.add(entryWidget);

        return widgets;
    }

    @Override
    public int getDisplayHeight()
    {
        return 100;
    }

    @Override
    public int getDisplayWidth(TransformingToolDisplay display)
    {
        return 160;
    }

    public static class ToolWidget extends Widget
    {
        private final Point origin;
        private final Component name;
        private final Font textRenderer = Minecraft.getInstance().font;
        private final int width;
        private final Widget widget;

        public ToolWidget(Point origin, int width, ResourceLocation texture)
        {
            var step = new CombineStep(ItemVariant.BLANK);
            this.origin = origin;
            this.name = step.getName();
            this.width = width;

            this.widget = Widgets.wrapVanillaWidget(new ImageButton(
                    origin.x + width() - 14, origin.y, 16, 16, 0, 0, 0,
                    texture, 16, 16, b -> {}));
        }

        public int height()
        {
            return Math.max(textRenderer.lineHeight + 3, 19);
        }

        public int width()
        {
            return width;
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return List.of(widget);
        }

        @Override
        public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
        {
            int x = origin.x + 2;
            int y = origin.y + 2;

            GUIUtil.drawText(matrices, textRenderer, name, x, y, PLCCols.BORDER.col, true);

            GUIUtil.renderBorder(matrices, origin.x, origin.y, width() + 3, height(), PLCCols.BORDER.col, 0);
            widget.render(matrices, mouseX, mouseY, delta);
        }
    }
}
