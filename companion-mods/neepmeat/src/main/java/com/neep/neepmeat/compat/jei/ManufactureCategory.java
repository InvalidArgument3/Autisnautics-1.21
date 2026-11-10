/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.compat.jei;

import com.neep.neepmeat.machine.NeepMachines;
import com.neep.neepmeat.recipe.ManufactureRecipe;
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

public class ManufactureCategory implements IRecipeCategory<RecipeHolder<ManufactureRecipe>> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final RecipeType<RecipeHolder<ManufactureRecipe>> TYPE =
            RecipeType.create("neepmeat", "manufacture", (Class) RecipeHolder.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ManufactureCategory(IGuiHelper gui) {
        this.background = gui.createBlankDrawable(140, 36);
        this.icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(NeepMachines.BUILDER_ITEM.get()));
    }

    @Override
    public RecipeType<RecipeHolder<ManufactureRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("category.neepmeat.manufacture");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ManufactureRecipe> holder, IFocusGroup focuses) {
        ManufactureRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 8).addItemStack(recipe.baseStack());
        int x = 26;
        for (ManufactureRecipe.ManufactureStepSpec step : recipe.steps()) {
            ItemStack icon = step.icon();
            if (!icon.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.INPUT, x, 8).addItemStack(icon);
                x += 18;
            }
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 8).addItemStack(recipe.resultStack());
    }
}
