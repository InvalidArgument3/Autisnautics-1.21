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

package com.neep.neepmeat.compat.emi.recipe;

import com.google.common.collect.Lists;
import com.neep.meatlib.recipe.ingredient.entity.EntityRecipeInput;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.compat.emi.NMEmiPlugin;
import com.neep.neepmeat.compat.emi.helper.EmiIngredientHelper;
import com.neep.neepmeat.plc.recipe.EntityToItemRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class EntityToItemManufactureEmiRecipe extends ManufactureEmiRecipe<EntityRecipeInput>
{
    private final ResourceLocation id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;

    public EntityToItemManufactureEmiRecipe(EntityToItemRecipe recipe)
    {
        super(recipe.getBase(), recipe.getSteps());

        List<EmiIngredient> inputs = Lists.newArrayList();
        appendStepIngredients(steps, inputs);

        this.id = recipe.getId();
        this.input = inputs;
        this.output = List.of(EmiIngredientHelper.outputToStack(recipe.getRecipeOutput()));
    }

    public static int borderCol()
    {
        return PLCCols.BORDER.col;
    }

    @Override
    public EmiRecipeCategory getCategory()
    {
        return NMEmiPlugin.ITEM_MANUFACTURE;
    }

    @Override
    public @Nullable ResourceLocation getId()
    {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs()
    {
        return input;
    }

    @Override
    public List<EmiStack> getOutputs()
    {
        return output;
    }

    @Override
    public void addWidgets(WidgetHolder widgets)
    {
        int startX = 5;
        int startY = 5;

        widgets.add(new OutlineWidget(new Bounds(0, 0, getDisplayWidth(), getDisplayHeight())));

        var widgetBase = new LabelledEntitySlot(startX, startY, Component.nullToEmpty("Base: "), base, widgets);
        widgets.add(widgetBase);

        var widgetOutput = new LabelledSlot(startX + 20 + 18 + widgetBase.width(), startY + 18, Component.nullToEmpty("Output: "), output.get(0), widgets, this);
        widgets.add(widgetOutput);

        int entryX = startX + 1;
        int entryY = startY + 22 + 18;
        for (var step : steps)
        {
            var widget = new EntryWidget(entryX, entryY, step, getDisplayWidth() - 20, widgets);
            widgets.add(widget);
            entryY += widget.height() + 2;
        }
    }

    static class LabelledEntitySlot extends Widget
    {
        private final int originX;
        private final int originY;
        private final EntityRecipeInput entityType;
        private final Component name;
        private final int slotOriginX;
        private final int slotOriginY;
        private final Font textRenderer = Minecraft.getInstance().font;

        public LabelledEntitySlot(int originX, int originY, Component name, EntityRecipeInput entityType, WidgetHolder widgets)
        {
            this.name = name;
            this.originX = originX;
            this.originY = originY;
            this.entityType = entityType;
            this.slotOriginX = originX + textRenderer.width(name) + 2;
            this.slotOriginY = originY;
        }

        public int height()
        {
            return Math.max(textRenderer.lineHeight + 3, 19);
        }

        public int width()
        {
            return textRenderer.width(name) + 2 + 20;
        }

        @Override
        public Bounds getBounds()
        {
            return new Bounds(originX, originY, width(), height());
        }

        @Override
        public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
        {
            matrices.drawString(textRenderer, name, originX, originY, ManufactureEmiRecipe.borderCol(), true);
            matrices.drawString(textRenderer, entityType.format(), slotOriginX, slotOriginY, ManufactureEmiRecipe.borderCol(), true);
//            GUIUtil.renderBorder(matrices, slotOriginX, slotOriginY, 17, 17, ItemManufactureEmiRecipe.borderCol(), 0);
//            GUIUtil.renderBorder(matrices, slotOriginX + 1, slotOriginY + 1, 15, 15, PLCCols.TRANSPARENT.col, 0);
        }
    }
}
