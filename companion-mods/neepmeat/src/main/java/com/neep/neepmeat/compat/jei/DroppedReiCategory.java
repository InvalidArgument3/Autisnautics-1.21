/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * The REI category id, with no recipes until that serializer is ported.
 */

package com.neep.neepmeat.compat.jei;

import com.neep.neepmeat.machine.NeepMachines;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class DroppedReiCategory implements IRecipeCategory<ItemStack> {
    private final RecipeType<ItemStack> type;
    private final IDrawable icon;

    public DroppedReiCategory(IGuiHelper gui, String id) {
        this.type = RecipeType.create("neepmeat", id, ItemStack.class);
        this.icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(NeepMachines.BUILDER_ITEM.get()));
    }

    @Override
    public RecipeType<ItemStack> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("category.neepmeat." + type.getUid().getPath());
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 100;
    }

    @Override
    public int getHeight() {
        return 36;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ItemStack recipe, IFocusGroup focuses) {}
}
