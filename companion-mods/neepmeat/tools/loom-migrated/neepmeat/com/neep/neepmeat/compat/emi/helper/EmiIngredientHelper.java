package com.neep.neepmeat.compat.emi.helper;

import com.neep.meatlib.recipe.ingredient.Ingredients;
import com.neep.meatlib.recipe.ingredient.RecipeOutput;
import com.neep.meatlib.recipe.ingredient.RegistryRecipeInput;
import com.neep.neepmeat.init.NMrecipeTypes;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import java.util.ArrayList;
import java.util.List;

public class EmiIngredientHelper
{
    public static final EmiIngredient EMPTY = EmiIngredient.of(Ingredient.EMPTY);
    public static final List<EmiIngredient> EMPTY_LIST = List.of(EMPTY);

    public static EmiIngredient inputToIngredient(RegistryRecipeInput<?> input)
    {
        if (input.isEmpty()) return EMPTY;

        long amount = input.amount();
        CompoundTag nbt = input.nbtMatcher().getNbt();

        if (Ingredients.ITEM.equals(input.getType()))
        {
            List<Item> items = (List<Item>) input.getAll();
            if (items.isEmpty()) return EMPTY;

            return EmiIngredient.of(
                    items.stream().<EmiIngredient>map(item -> EmiStack.of(item, nbt, amount)).toList(),
                    amount
            );
        }

        if (Ingredients.FLUID.equals(input.getType()))
        {
            List<Fluid> fluids = (List<Fluid>) input.getAll();
            if (fluids.isEmpty()) return EMPTY;

            return EmiIngredient.of(
                    fluids.stream().map(fluid -> (EmiIngredient) EmiStack.of(fluid, nbt, input.amount())).toList(),
                    amount);
        }

        return EmiIngredient.of(List.of()); // TODO: uh oh
    }

    public static EmiStack outputToStack(RecipeOutput<Item> output)
    {
        EmiStack emiStack = new ItemEmiStack(output.resource(), output.getNbt(), output.minAmount()) {
			@Override
			public List<Component> getTooltipText() {
				List<Component> tooltip = new ArrayList<>(super.getTooltipText());
				tooltip.add(NMrecipeTypes.ofMinMax(output.minAmount(), output.maxAmount()));
				return tooltip;
			}
		};

        if (output.chance() != 1)
            emiStack.setChance(output.chance());

        return emiStack;
    }

    public static EmiStack outputFluidToStack(RecipeOutput<Fluid> output)
    {
        EmiStack emiStack = new FluidEmiStack(output.resource(), output.getNbt(), output.minAmount()) {
			@Override
			public List<Component> getTooltipText() {
				List<Component> tooltip = new ArrayList<>(super.getTooltipText());
				tooltip.add(NMrecipeTypes.ofMinMax(output.minAmount(), output.maxAmount()));
				return tooltip;
			}
		};

        if (output.chance() != 1)
            emiStack.setChance(output.chance());

        return emiStack;
    }

    public static EmiIngredient variantToIngredient(ItemVariant variant)
    {
        return EmiStack.of(variant.getItem(), variant.getNbt());
    }
}
