package com.neep.neepmeat.compat.emi.recipe;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.data.DataUtil;
import com.neep.neepmeat.compat.emi.NMEmiPlugin;
import com.neep.neepmeat.compat.emi.helper.EmiIngredientHelper;
import com.neep.neepmeat.recipe.EnlighteningRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class EnlighteningEmiRecipe implements EmiRecipe {
    private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/enlightenment.png");

    private final ResourceLocation id;
    private final EmiIngredient input;
    private final List<EmiStack> output;

    private final long data;

    public EnlighteningEmiRecipe(net.minecraft.world.item.crafting.RecipeHolder<EnlighteningRecipe> holder) {
        EnlighteningRecipe recipe = holder.value();
        this.data = recipe.getData();

        this.id = holder.id();
        this.input = EmiIngredientHelper.inputToIngredient(recipe.getItemInput());
        this.output = List.of(EmiIngredientHelper.outputToStack(recipe.getItemOutput()));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return NMEmiPlugin.ENLIGHTENING;
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
        int startX = getDisplayWidth() / 2 - 41;
        int startY = 10;

        widgets.addText(Component.translatable("category." + NeepMeat.NAMESPACE + ".enlightening.data", DataUtil.formatData(data)), getDisplayWidth() - 5, 5, 0xFF404040, false).horizontalAlign(TextWidget.Alignment.END);

        widgets.addTexture(EmiTexture.EMPTY_ARROW, startX + 25, startY + 9);

        widgets.addSlot(input, startX + 1, startY + 3);

        widgets.addSlot(output.get(0), startX + 61, startY + 9).recipeContext(this);

        int thingWidth = 44;
        widgets.addTexture(new EmiTexture(texture, 0, 0, thingWidth, thingWidth, thingWidth, thingWidth, thingWidth, thingWidth), startX - 24, startY - 4);

        if (output.size() > 1) {
            widgets.addSlot(output.get(1), startX + 81, startY + 9).recipeContext(this);
        }
    }
}
