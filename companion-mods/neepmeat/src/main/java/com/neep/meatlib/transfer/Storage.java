/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.transfer;

public interface Storage<T> extends Iterable<StorageView<T>> {
    default Iterable<StorageView<T>> nonEmptyViews() {
        java.util.List<StorageView<T>> views = new java.util.ArrayList<>();
        for (StorageView<T> view : this) {
            if (view.getAmount() > 0) {
                views.add(view);
            }
        }
        return views;
    }

    long insert(T resource, long maxAmount, TransactionContext transaction);

    long extract(T resource, long maxAmount, TransactionContext transaction);

    static <T> Storage<T> empty() {
        return new Storage<>() {
            @Override
            public long insert(T resource, long maxAmount, TransactionContext transaction) {
                return 0;
            }

            @Override
            public long extract(T resource, long maxAmount, TransactionContext transaction) {
                return 0;
            }

            @Override
            public java.util.Iterator<StorageView<T>> iterator() {
                return java.util.List.<StorageView<T>>of().iterator();
            }
        };
    }
}
