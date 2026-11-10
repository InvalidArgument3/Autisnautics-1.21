/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * The slot accepts a stack and adds the machine's progress increment.
 * Advanced-crushing recipes are not loaded, so a finished stack is not replaced.
 */

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.live_machine.LiveMachines;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CrusherSegmentBlockEntity extends BlockEntity implements LivingMachineComponent {
    private ItemStack stack = ItemStack.EMPTY;
    private float progress;
    private int recipeTime;

    public CrusherSegmentBlockEntity(BlockPos pos, BlockState state) {
        super(LiveMachines.CRUSHER_BE.get(), pos, state);
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

    public void assignCobblestoneRecipe() {
        if (stack.is(net.minecraft.world.item.Items.COBBLESTONE)) {
            recipeTime = 1;
        }
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
        return new ItemStack(net.minecraft.world.item.Items.GRAVEL);
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> componentType() {
        return LivingMachineComponents.CRUSHER_SEGMENT;
    }
}
