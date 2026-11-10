package com.neep.neepmeat.compat.emi.recipe;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.compat.emi.helper.EmiIngredientHelper;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.recipe.CrushingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class GrindingEmiRecipe implements EmiRecipe {
    private final ResourceLocation id;
    private final EmiIngredient input;
    private final List<EmiStack> output;

    private final EmiRecipeCategory category;
    private final CrushingRecipe recipe;

    public GrindingEmiRecipe(EmiRecipeCategory category, CrushingRecipe recipe) {
        this.category = category;
        this.recipe = recipe;

        this.id = recipe.getId();
//        this.input = List.of(EmiIngredient.of(recipe.getItemInput().getAll().stream().map(Ingredient::ofItems).map(EmiIngredient::of).toList(), recipe.getItemInput().amount()));
        this.input = EmiIngredientHelper.inputToIngredient(recipe.getItemInput());

        List<EmiStack> output = new ArrayList<>();

        if (!recipe.destroy())
        {
            output.add(EmiIngredientHelper.outputToStack(recipe.getItemOutput()));
            if (recipe.getAuxOutput() != null) {
                output.add(EmiIngredientHelper.outputToStack(recipe.getAuxOutput()));
            }
        }

        this.output = output;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return output;
    }

    @Override
    public int getDisplayWidth() {
        return 150;
    }

    @Override
    public int getDisplayHeight() {
        return 55;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        if (recipe.destroy())
        {
            return;
        }

        int startX = getDisplayWidth() / 2 - 41;
        int startY = 10;

        widgets.addText(NeepMeat.translationKey("category", "general.energy", PowerUtil.energyForRecipe(recipe.getTime())), getDisplayWidth() - 5, 3, 0xFF404040, false).horizontalAlign(TextWidget.Alignment.END);

        widgets.addTexture(EmiTexture.EMPTY_ARROW, startX + 25, startY + 9);

        widgets.addSlot(input, startX + 1, startY + 9);

        widgets.addSlot(output.get(0), startX + 61, startY + 9)
                .appendTooltip(NMrecipeTypes.ofMinMax(recipe.getItemOutput().minAmount(), recipe.getItemOutput().maxAmount())).recipeContext(this);

        if (output.size() > 1) {
            widgets.addSlot(output.get(1), startX + 81, startY + 9).recipeContext(this);
        }
    }
}
