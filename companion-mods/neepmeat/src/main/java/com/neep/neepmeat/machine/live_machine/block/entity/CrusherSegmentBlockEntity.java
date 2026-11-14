/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * The slot accepts a stack and adds the machine's progress increment.
 * Advanced crushing is tried first. Grinding is the fallback. The destroy recipe never matches.
 */

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.neepmeat.recipe.CrushingRecipe;
import com.neep.neepmeat.recipe.NeepRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CrusherSegmentBlockEntity extends BlockEntity implements LivingMachineComponent {
    private ItemStack stack = ItemStack.EMPTY;
    private float progress;
    private int recipeTime;
    private @Nullable CrushingRecipe recipe;

    public CrusherSegmentBlockEntity(BlockPos pos, BlockState state) {
        super(LivingMachines.CRUSHER_BE.get(), pos, state);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public ItemStack stack() {
        return stack;
    }

    public void insert(ItemStack stack) {
        this.stack = stack;
    }

    public float progress() {
        return progress;
    }

    public void assignRecipe() {
        recipe = MeatlibRecipes.getInstance().matchCrushing(NeepRecipes.ADVANCED_CRUSHING.get(), stack)
                .or(() -> MeatlibRecipes.getInstance().matchCrushing(NeepRecipes.GRINDING.get(), stack))
                .orElse(null);
        recipeTime = recipe == null ? 0 : Math.max(1, recipe.time());
    }

    public boolean addProgress(float amount) {
        if (stack.isEmpty() || recipeTime <= 0) {
            return false;
        }
        progress += amount;
        return progress >= recipeTime;
    }

    public ItemStack takeResult() {
        stack = ItemStack.EMPTY;
        progress = 0;
        recipeTime = 0;
        return recipe == null ? ItemStack.EMPTY : recipe.rollOutput(RandomSource.create());
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType() {
        return LivingMachineComponents.CRUSHER_SEGMENT;
    }
}
