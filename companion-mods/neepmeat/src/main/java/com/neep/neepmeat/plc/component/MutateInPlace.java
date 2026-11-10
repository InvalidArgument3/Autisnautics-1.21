/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.component;

import com.neep.neepmeat.recipe.ManufactureRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface MutateInPlace<T> {
    Lookup<ItemStack> ITEM = MutateInPlace::findItem;
    Lookup<Entity> ENTITY = (level, pos, direction) -> null;

    @Nullable T get();

    void set(T value);

    default void store(int consume, T value) {
        set(value);
    }

    static @Nullable MutateInPlace<ItemStack> findItem(Level level, BlockPos pos, @Nullable Direction direction) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof Container container)) {
            return null;
        }
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (!container.getItem(slot).isEmpty()) {
                int found = slot;
                return ofItem(container.getItem(found), stack -> container.setItem(found, stack), level, pos);
            }
        }
        return null;
    }

    static MutateInPlace<ItemStack> ofItem(ItemStack stack, Consumer<ItemStack> store, Level level, BlockPos pos) {
        return new Slot(stack, store, level, pos);
    }

    default ItemStack combine(ItemStack ingredient, RecipeManager recipes) {
        ItemStack current = (ItemStack) get();
        ItemStack next = ManufactureRecipe.apply(current, new ManufactureRecipe.ManufactureStepSpec(
                "neepmeat:combine", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(ingredient.getItem()).toString()), recipes);
        set(cast(next));
        return next;
    }

    @SuppressWarnings("unchecked")
    private T cast(ItemStack stack) {
        return (T) stack;
    }

    @FunctionalInterface
    interface Lookup<T> {
        @Nullable MutateInPlace<T> find(Level level, BlockPos pos, @Nullable Direction direction);
    }

    final class Slot implements MutateInPlace<ItemStack> {
        private ItemStack stack;
        private final Consumer<ItemStack> store;
        private final Level level;
        private final BlockPos pos;

        private Slot(ItemStack stack, Consumer<ItemStack> store, Level level, BlockPos pos) {
            this.stack = stack;
            this.store = store;
            this.level = level;
            this.pos = pos;
        }

        @Override
        public ItemStack get() {
            return stack;
        }

        @Override
        public void set(ItemStack value) {
            this.stack = value;
            store.accept(value);
        }

        public Level level() {
            return level;
        }

        public BlockPos pos() {
            return pos;
        }
    }
}
