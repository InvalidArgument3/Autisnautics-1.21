/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 * Fabric InventoryStorage shape over a vanilla Container. Amounts are item counts.
 */

package com.neep.meatlib.transfer;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class InventoryStorage implements Storage<ItemVariant> {
    private final Container inventory;

    private InventoryStorage(Container inventory) {
        this.inventory = inventory;
    }

    public static InventoryStorage of(Container inventory, @Nullable Direction side) {
        return new InventoryStorage(inventory);
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        int left = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        int moved = 0;
        List<Runnable> rollback = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize() && left > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            int before = stack.getCount();
            if (stack.isEmpty()) {
                int place = Math.min(left, resource.getItem().getDefaultMaxStackSize());
                inventory.setItem(slot, resource.toStack(place));
                int index = slot;
                rollback.add(() -> inventory.setItem(index, ItemStack.EMPTY));
                left -= place;
                moved += place;
            } else if (stack.is(resource.getItem()) && stack.getCount() < stack.getMaxStackSize()) {
                int place = Math.min(left, stack.getMaxStackSize() - stack.getCount());
                stack.grow(place);
                int index = slot;
                int previous = before;
                rollback.add(() -> inventory.getItem(index).setCount(previous));
                left -= place;
                moved += place;
            }
        }
        if (transaction instanceof Transaction open) {
            open.snapshot(() -> {
                for (int i = rollback.size() - 1; i >= 0; i--) {
                    rollback.get(i).run();
                }
            });
        }
        return moved;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        int left = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        int moved = 0;
        List<Runnable> rollback = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize() && left > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !stack.is(resource.getItem())) {
                continue;
            }
            int take = Math.min(left, stack.getCount());
            int before = stack.getCount();
            stack.shrink(take);
            if (stack.isEmpty()) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
            int index = slot;
            int previous = before;
            ItemStack previousStack = stack.copy();
            previousStack.setCount(previous);
            rollback.add(() -> inventory.setItem(index, previousStack));
            left -= take;
            moved += take;
        }
        if (transaction instanceof Transaction open) {
            open.snapshot(() -> {
                for (int i = rollback.size() - 1; i >= 0; i--) {
                    rollback.get(i).run();
                }
            });
        }
        return moved;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        List<StorageView<ItemVariant>> views = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            views.add(new SlotView(ItemVariant.of(stack), stack.getCount(), stack.getMaxStackSize()));
        }
        return views.iterator();
    }

    private record SlotView(ItemVariant resource, long amount, long capacity) implements StorageView<ItemVariant> {
        @Override
        public ItemVariant getResource() {
            return resource;
        }

        @Override
        public long getAmount() {
            return amount;
        }

        @Override
        public long getCapacity() {
            return capacity;
        }
    }
}
