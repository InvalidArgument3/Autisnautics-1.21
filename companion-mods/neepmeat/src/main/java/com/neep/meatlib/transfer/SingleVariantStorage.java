/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 * Amounts are Fabric droplets. A NeoForge handler sees whole millibuckets, 81 droplets each.
 */

package com.neep.meatlib.transfer;

import java.util.Collections;
import java.util.Iterator;

public abstract class SingleVariantStorage<T> implements Storage<T>, StorageView<T> {
    public T variant;
    public long amount;

    protected SingleVariantStorage(T blank) {
        this.variant = blank;
    }

    protected abstract T blank();

    protected abstract long capacity(T resource);

    protected abstract boolean matches(T stored, T offered);

    protected void onFinalCommit() {}

    @Override
    public T getResource() {
        return variant;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public long getCapacity() {
        return capacity(variant);
    }

    @Override
    public boolean isResourceBlank() {
        return amount <= 0 || matches(variant, blank());
    }

    @Override
    public long insert(T resource, long maxAmount, TransactionContext transaction) {
        if (maxAmount <= 0 || resource == null || matches(resource, blank())) {
            return 0;
        }
        if (amount > 0 && !matches(variant, resource)) {
            return 0;
        }
        long room = capacity(resource) - amount;
        long moved = Math.min(room, maxAmount);
        if (moved <= 0) {
            return 0;
        }
        T previousVariant = variant;
        long previousAmount = amount;
        variant = resource;
        amount += moved;
        if (transaction instanceof Transaction open) {
            open.snapshot(() -> {
                variant = previousVariant;
                amount = previousAmount;
            });
        }
        return moved;
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction) {
        if (maxAmount <= 0 || amount <= 0 || !matches(variant, resource)) {
            return 0;
        }
        long moved = Math.min(amount, maxAmount);
        T previousVariant = variant;
        long previousAmount = amount;
        amount -= moved;
        if (amount == 0) {
            variant = blank();
        }
        if (transaction instanceof Transaction open) {
            open.snapshot(() -> {
                variant = previousVariant;
                amount = previousAmount;
            });
        }
        return moved;
    }

    @Override
    public Iterator<StorageView<T>> iterator() {
        return Collections.<StorageView<T>>singleton(this).iterator();
    }
}
