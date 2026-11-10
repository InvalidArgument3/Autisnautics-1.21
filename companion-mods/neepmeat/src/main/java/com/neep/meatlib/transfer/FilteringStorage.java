/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.transfer;

import java.util.function.Predicate;

public final class FilteringStorage<T> implements Storage<T> {
    private final Storage<T> backing;
    private final Predicate<T> filter;

    private FilteringStorage(Storage<T> backing, Predicate<T> filter) {
        this.backing = backing;
        this.filter = filter;
    }

    public static <T> FilteringStorage<T> insertOnly(Storage<T> backing, Predicate<T> filter) {
        return new FilteringStorage<>(backing, filter);
    }

    @Override
    public long insert(T resource, long maxAmount, TransactionContext transaction) {
        return filter.test(resource) ? backing.insert(resource, maxAmount, transaction) : 0;
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction) {
        return backing.extract(resource, maxAmount, transaction);
    }

    @Override
    public java.util.Iterator<StorageView<T>> iterator() {
        return backing.iterator();
    }
}
