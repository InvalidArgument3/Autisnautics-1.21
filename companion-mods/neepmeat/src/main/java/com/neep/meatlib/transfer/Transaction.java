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
    private boolean open = true;

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

    @Override
    public void onRename() {
        if (!open) {
            return;
        }
        open = false;
        if (parent == null) {
            CURRENT.remove();
        } else {
            parent.rollbacks.addAll(rollbacks);
        }
        rollbacks.clear();
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
