package com.neep.neepmeat.compat.emi.recipe;

import com.google.common.collect.Lists;
import com.neep.neepmeat.compat.emi.NMEmiPlugin;
import com.neep.neepmeat.compat.emi.helper.EmiIngredientHelper;
import com.neep.neepmeat.plc.recipe.*;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemManufactureEmiRecipe extends ManufactureEmiRecipe<Item>
{
    private final ResourceLocation id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;

    public ItemManufactureEmiRecipe(ItemManufactureRecipe recipe)
    {
        super(recipe.getBase(), recipe.getSteps());

        List<EmiIngredient> inputs = Lists.newArrayList();
        appendStepIngredients(steps, inputs);
        inputs.add(EmiStack.of(recipe.getBase()));

        this.id = recipe.getId();
        this.input = inputs;
//        this.output = List.of(EmiStack.of(recipe.getRecipeOutput().resource(), recipe.getRecipeOutput().minAmount()));
        this.output = Collections.singletonList(EmiIngredientHelper.outputToStack(recipe.getRecipeOutput()));
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

        var widgetBase = new LabelledSlot(startX, startY, Component.nullToEmpty("Base: "), EmiStack.of(getBase()), widgets);
        widgets.add(widgetBase);

        var widgetOutput = new LabelledSlot(startX + 20 + widgetBase.width(), startY, Component.nullToEmpty("Output: "), output.get(0), widgets, this);
        widgets.add(widgetOutput);

        int entryX = startX + 1;
        int entryY = startY + 22;
        for (var step : steps)
        {
            var widget = new EntryWidget(entryX, entryY, step, getDisplayWidth() - 20, widgets);
            widgets.add(widget);
            entryY += widget.height() + 2;
        }
    }
}
