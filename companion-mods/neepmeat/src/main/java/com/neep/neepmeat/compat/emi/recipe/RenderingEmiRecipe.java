package com.neep.neepmeat.compat.emi.recipe;

import com.neep.neepmeat.compat.emi.NMEmiPlugin;
import com.neep.neepmeat.compat.emi.helper.EmiIngredientHelper;
import com.neep.neepmeat.recipe.RenderingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class RenderingEmiRecipe implements EmiRecipe
{
    private final ResourceLocation id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;
    private final RenderingRecipe recipe;

    public RenderingEmiRecipe(RenderingRecipe recipe)
    {
        this.id = recipe.getId();
        this.input = List.of(
                EmiIngredientHelper.inputToIngredient(recipe.getFluidInput()),
                EmiIngredientHelper.inputToIngredient(recipe.getItemInput()));
        this.output = List.of(EmiIngredientHelper.outputFluidToStack(recipe.getFluidOutput()));
        this.recipe = recipe;
    }

    @Override
    public EmiRecipeCategory getCategory()
    {
        return NMEmiPlugin.RENDERING;
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
    public int getDisplayWidth()
    {
        return 150;
    }

    @Override
    public int getDisplayHeight()
    {
        return 55;
    }

    @Override
    public void addWidgets(WidgetHolder widgets)
    {
        int startX = getDisplayWidth() / 2 - 51;
        int startY = 10;

        widgets.addTexture(EmiTexture.EMPTY_ARROW, startX + 42, startY + 9);

        // Input slot
        widgets.addSlot(input.get(0), startX + 1, startY + 9);
        widgets.addSlot(input.get(1), startX + 1 + 19, startY + 9);

        // Output slot
        widgets.addSlot(output.get(0), startX + 70, startY + 9).recipeContext(this);
    }
}
