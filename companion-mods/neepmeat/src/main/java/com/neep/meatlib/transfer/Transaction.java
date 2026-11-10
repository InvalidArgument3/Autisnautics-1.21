/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.transfer;

import java.util.ArrayList;
import java.util.List;

public final class Transaction implements TransactionContext, AutoCloseable {
    private static final ThreadLocal<Transaction> CURRENT = new ThreadLocal<>();

    private final Transaction parent;
    private final List<Runnable> rollbacks = new ArrayList<>();
    private final List<Runnable> outerCommits = new ArrayList<>();
    private final List<SlotDelta> slotDeltas = new ArrayList<>();
    private boolean open = true;

    /** Net change reserved against a handler slot. Positive is an insert. */
    public record SlotDelta(Object target, int index, Object variant, int delta) {}

    private Transaction(Transaction parent) {
        this.parent = parent;
    }

    public static Transaction openOuter() {
        if (CURRENT.get() != null) {
            throw new IllegalStateException("an outer transaction is already open");
        }
        Transaction transaction = new Transaction(null);
        CURRENT.set(transaction);
        return transaction;
    }

    public Transaction openNested() {
        if (!open) {
            throw new IllegalStateException("transaction is closed");
        }
        return new Transaction(this);
    }

    public void snapshot(Runnable rollback) {
        if (!open) {
            throw new IllegalStateException("transaction is closed");
        }
        rollbacks.add(rollback);
    }

    /**
     * Runs only if the outermost transaction commits. Nested abort discards it.
     * Nested commit moves it to the parent.
     */
    public void addOuterCommit(Runnable action) {
        if (!open) {
            throw new IllegalStateException("transaction is closed");
        }
        outerCommits.add(action);
    }

    public void reserve(Object target, int index, Object variant, int delta) {
        if (!open) {
            throw new IllegalStateException("transaction is closed");
        }
        slotDeltas.add(new SlotDelta(target, index, variant, delta));
    }

    public int slotDelta(Object target, int index) {
        int sum = 0;
        for (Transaction cursor = this; cursor != null; cursor = cursor.parent) {
            for (SlotDelta delta : cursor.slotDeltas) {
                if (delta.target == target && delta.index == index) {
                    sum += delta.delta;
                }
            }
        }
        return sum;
    }

    public Object slotVariant(Object target, int index) {
        Object variant = null;
        for (Transaction cursor = this; cursor != null; cursor = cursor.parent) {
            for (SlotDelta delta : cursor.slotDeltas) {
                if (delta.target == target && delta.index == index && delta.variant != null) {
                    variant = delta.variant;
                }
            }
        }
        return variant;
    }

    public void commit() {
        onRename();
    }

    @Override
    public void onRename() {
        if (!open) {
            return;
        }
        open = false;
        if (parent == null) {
            CURRENT.remove();
            for (Runnable action : outerCommits) {
                action.run();
            }
        } else {
            parent.rollbacks.addAll(rollbacks);
            parent.outerCommits.addAll(outerCommits);
            parent.slotDeltas.addAll(slotDeltas);
        }
        rollbacks.clear();
        outerCommits.clear();
        slotDeltas.clear();
    }

    @Override
    public void abort() {
        if (!open) {
            return;
        }
        open = false;
        for (int index = rollbacks.size() - 1; index >= 0; index--) {
            rollbacks.get(index).run();
        }
        rollbacks.clear();
        outerCommits.clear();
        slotDeltas.clear();
        if (parent == null) {
            CURRENT.remove();
        }
    }

    @Override
    public void close() {
        if (open) {
            abort();
        }
    }
}
