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
import java.util.Iterator;
import java.util.List;

public class CombinedStorage<T> implements Storage<T> {
    private final List<Storage<T>> parts;

    public CombinedStorage(List<Storage<T>> parts) {
        this.parts = parts;
    }

    @Override
    public long insert(T resource, long maxAmount, TransactionContext transaction) {
        long left = maxAmount;
        for (Storage<T> part : parts) {
            if (left <= 0) {
                break;
            }
            left -= part.insert(resource, left, transaction);
        }
        return maxAmount - left;
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction) {
        long left = maxAmount;
        for (Storage<T> part : parts) {
            if (left <= 0) {
                break;
            }
            left -= part.extract(resource, left, transaction);
        }
        return maxAmount - left;
    }

    @Override
    public Iterator<StorageView<T>> iterator() {
        List<StorageView<T>> views = new ArrayList<>();
        for (Storage<T> part : parts) {
            part.forEach(views::add);
        }
        return views.iterator();
    }
}
