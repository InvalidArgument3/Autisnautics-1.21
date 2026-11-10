/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Amounts are item counts. The handler is only changed when the outer transaction commits.
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
        if (!(transaction instanceof Transaction open) || resource == null || resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        long left = maxAmount;
        List<int[]> plan = new ArrayList<>();
        for (int slot = 0; slot < handler.getSlots() && left > 0; slot++) {
            int space = insertSpace(open, slot, resource);
            if (space <= 0) {
                continue;
            }
            int offered = (int) Math.min(left, Math.min(space, stackFor(resource, left).getCount()));
            ItemStack rejected = handler.insertItem(slot, stackFor(resource, offered), true);
            int accepted = offered - rejected.getCount();
            if (accepted <= 0) {
                continue;
            }
            plan.add(new int[] {slot, accepted});
            open.reserve(handler, slot, resource, accepted);
            left -= accepted;
        }
        if (plan.isEmpty()) {
            return 0;
        }
        open.addOuterCommit(() -> {
            for (int[] move : plan) {
                handler.insertItem(move[0], stackFor(resource, move[1]), false);
            }
        });
        return maxAmount - left;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (!(transaction instanceof Transaction open) || resource == null || resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        long left = maxAmount;
        List<int[]> plan = new ArrayList<>();
        for (int slot = 0; slot < handler.getSlots() && left > 0; slot++) {
            int available = extractable(open, slot, resource);
            if (available <= 0) {
                continue;
            }
            int want = (int) Math.min(left, available);
            ItemStack simulated = handler.extractItem(slot, want, true);
            int accepted = Math.min(want, simulated.getCount());
            if (accepted <= 0 && available > 0 && handler.getStackInSlot(slot).isEmpty()) {
                accepted = want;
            }
            if (accepted <= 0) {
                continue;
            }
            plan.add(new int[] {slot, accepted});
            open.reserve(handler, slot, resource, -accepted);
            left -= accepted;
        }
        if (plan.isEmpty()) {
            return 0;
        }
        open.addOuterCommit(() -> {
            for (int[] move : plan) {
                handler.extractItem(move[0], move[1], false);
            }
        });
        return maxAmount - left;
    }

    private int insertSpace(Transaction transaction, int slot, ItemVariant resource) {
        ItemStack stack = handler.getStackInSlot(slot);
        int count = stack.getCount() + transaction.slotDelta(handler, slot);
        int limit = Math.min(handler.getSlotLimit(slot), resource.toStack().getMaxStackSize());
        if (count <= 0) {
            return limit;
        }
        ItemVariant present = stack.isEmpty() ? (ItemVariant) transaction.slotVariant(handler, slot) : ItemVariant.of(stack);
        if (present == null || !present.equals(resource)) {
            return 0;
        }
        return Math.max(0, limit - count);
    }

    private int extractable(Transaction transaction, int slot, ItemVariant resource) {
        ItemStack stack = handler.getStackInSlot(slot);
        int count = stack.getCount() + transaction.slotDelta(handler, slot);
        if (count <= 0) {
            return 0;
        }
        ItemVariant present = stack.isEmpty() ? (ItemVariant) transaction.slotVariant(handler, slot) : ItemVariant.of(stack);
        return present != null && present.equals(resource) ? count : 0;
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
