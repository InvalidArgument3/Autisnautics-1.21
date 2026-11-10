/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * One item and a data counter. A matching enlightening recipe spends its data cost.
 */

package com.neep.neepmeat.machine.integrator;

import com.neep.neepmeat.machine.StageMachines;
import com.neep.neepmeat.recipe.EnlighteningRecipe;
import com.neep.neepmeat.recipe.NeepRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class IntegratorBlockEntity extends BlockEntity {
    private ItemStack stack = ItemStack.EMPTY;
    private int data;

    public IntegratorBlockEntity(BlockPos pos, BlockState state) {
        super(StageMachines.INTEGRATOR_BE.get(), pos, state);
    }

    public ItemStack stack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
        setChanged();
    }

    public int data() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
        setChanged();
    }

    public void serverTick() {
        if (level == null || stack.isEmpty() || data <= 0) {
            return;
        }
        for (var holder : level.getRecipeManager().getAllRecipesFor(NeepRecipes.ENLIGHTENING.get())) {
            EnlighteningRecipe recipe = holder.value();
            if (recipe.matches(stack) && data >= recipe.data()) {
                data -= recipe.data();
                stack.shrink(recipe.inputCount());
                stack = recipe.resultStack();
                setChanged();
                return;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("data", data);
        if (!stack.isEmpty()) {
            tag.put("item", stack.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        data = tag.getInt("data");
        stack = tag.contains("item") ? ItemStack.parse(registries, tag.getCompound("item")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
    }
}
