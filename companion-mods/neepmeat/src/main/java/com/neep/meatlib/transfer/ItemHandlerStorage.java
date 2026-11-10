/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Amounts are item counts. NeoForge handlers have no Fabric transaction.
 */

package com.neep.meatlib.transfer;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ItemHandlerStorage implements Storage<ItemVariant> {
    private final IItemHandler handler;

    private ItemHandlerStorage(IItemHandler handler) {
        this.handler = handler;
    }

    public static ItemHandlerStorage of(IItemHandler handler) {
        return new ItemHandlerStorage(handler);
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource == null || resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        long left = maxAmount;
        long inserted = 0;
        for (int slot = 0; slot < handler.getSlots() && left > 0; slot++) {
            ItemStack remaining = handler.insertItem(slot, stackFor(resource, left), false);
            long moved = Math.min(left, stackFor(resource, left).getCount()) - remaining.getCount();
            if (moved > 0) {
                inserted += moved;
                left -= moved;
            }
        }
        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource == null || resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        long left = maxAmount;
        long taken = 0;
        for (int slot = 0; slot < handler.getSlots() && left > 0; slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty() || !resource.equals(ItemVariant.of(stack))) {
                continue;
            }
            ItemStack extracted = handler.extractItem(slot, (int) Math.min(left, Integer.MAX_VALUE), false);
            taken += extracted.getCount();
            left -= extracted.getCount();
        }
        return taken;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        List<StorageView<ItemVariant>> views = new ArrayList<>();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            int index = slot;
            views.add(new StorageView<>() {
                @Override
                public ItemVariant getResource() {
                    return ItemVariant.of(handler.getStackInSlot(index));
                }

                @Override
                public long getAmount() {
                    return handler.getStackInSlot(index).getCount();
                }

                @Override
                public long getCapacity() {
                    return handler.getSlotLimit(index);
                }
            });
        }
        return views.iterator();
    }

    private static ItemStack stackFor(ItemVariant resource, long amount) {
        ItemStack stack = resource.toStack();
        stack.setCount((int) Math.min(amount, stack.getMaxStackSize()));
        return stack;
    }
}
