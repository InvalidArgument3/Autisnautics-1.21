/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.compat.jei;

import com.neep.neepmeat.machine.NeepMachines;
import com.neep.neepmeat.recipe.AlloyKilnRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class AlloyKilnCategory implements IRecipeCategory<RecipeHolder<AlloyKilnRecipe>> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final RecipeType<RecipeHolder<AlloyKilnRecipe>> TYPE =
            RecipeType.create("neepmeat", "alloy_smelting", (Class) RecipeHolder.class);

    private final IDrawable background;
    private final IDrawable icon;

    public AlloyKilnCategory(IGuiHelper gui) {
        this.background = gui.createBlankDrawable(100, 36);
        this.icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(NeepMachines.ALLOY_KILN_ITEM.get()));
    }

    @Override
    public RecipeType<RecipeHolder<AlloyKilnRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("category.neepmeat.alloy_smelting");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<AlloyKilnRecipe> holder, IFocusGroup focuses) {
        AlloyKilnRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 8).addItemStack(recipe.input1().stack());
        builder.addSlot(RecipeIngredientRole.INPUT, 26, 8).addItemStack(recipe.input2().stack());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 70, 8).addItemStack(recipe.output().stack());
    }
}
