package com.neep.neepmeat.compat.emi.recipe;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.compat.emi.NMEmiPlugin;
import com.neep.neepmeat.recipe.VivisectionRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public class VivisectionEmiRecipe implements EmiRecipe
{
    private final ResourceLocation id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;

    private final List<Component> entities;

    public VivisectionEmiRecipe(List<EntityType<?>> input, List<EmiStack> output)
    {
        this.entities = input.stream().map(e -> (Component) Component.translatable(e.getDescriptionId())).toList();

        this.id = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "/vivisection/" + String.join("_", input.stream().map(e -> e.toShortString().toLowerCase(Locale.ROOT)).toList()));
        this.input = List.of();
        this.output = output;
    }

    public VivisectionEmiRecipe(Item input, Item output)
    {
        this.entities = List.of();

        this.id = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "/vivisection/" + BuiltInRegistries.ITEM.getKey(input).getPath().toLowerCase(Locale.ROOT));
        this.input = List.of(EmiIngredient.of(Ingredient.of(input)));
        this.output = List.of(EmiStack.of(output));
    }

    public VivisectionEmiRecipe(VivisectionRecipe recipe)
    {
        this(List.of(recipe.getEntityType()), recipe.getRecipeOutput().stream().map(e -> EmiStack.of(e.resource(), e.minAmount())).toList());
    }

    @Override
    public EmiRecipeCategory getCategory()
    {
        return NMEmiPlugin.VIVISECTION;
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
        return 45;
    }

    @Override
    public void addWidgets(WidgetHolder widgets)
    {
        int startX = getDisplayWidth() / 2 - 41;
        int startY = 4;

        if (!entities.isEmpty())
        {
            widgets.addText(entities.get(0), 10, getDisplayHeight() / 2 - 5, 0xFFFFFF, true);
        }
        else if (!input.isEmpty())
        {
            widgets.addSlot(input.get(0), startX, startY + 9);
        }

        widgets.addTexture(EmiTexture.EMPTY_ARROW, startX + 25, startY + 9);

//        widgets.addSlot(output.get(0), startX + 61, startY + 9).recipeContext(this);

        int ny = Mth.ceil(output.size() / 3f);
//        int startX = getDisplayWidth() / 2 - 60;
//        int startY = (getDisplayHeight() - ny * 18) / 2 + 5;
//        int outputY = (getDisplayHeight() - 18) / 2;
        startX += 61;
        for (int j = 0; j < ny; ++j)
        {
            for (int i = 0; i < 3; ++i)
            {
                int y1 = startY + j * 18;
                if (output.size() <= 3)
                    y1 += 18 / 2;

                int idx = j * 3 + i;
                if (idx < output.size())
                {
                    EmiIngredient ingredient = output.get(idx);
                    widgets.addSlot(ingredient, startX + i * 18, y1).recipeContext(this);
                }
                else
                {
                    widgets.addSlot(startX + i * 18, y1);
                }
            }
        }
    }
}
