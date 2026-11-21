/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.transfer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CombinedSlottedStorage<T, S extends Storage<T>> implements Storage<T> {
    private final List<S> parts;

    public CombinedSlottedStorage(List<S> parts) {
        this.parts = parts;
    }

    public List<S> parts() {
        return parts;
    }

    @Override
    public long insert(T resource, long maxAmount, TransactionContext transaction) {
        long moved = 0;
        for (S part : parts) {
            moved += part.insert(resource, maxAmount - moved, transaction);
            if (moved >= maxAmount) {
                break;
            }
        }
        return moved;
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction) {
        long moved = 0;
        for (S part : parts) {
            moved += part.extract(resource, maxAmount - moved, transaction);
            if (moved >= maxAmount) {
                break;
            }
        }
        return moved;
    }

    @Override
    public Iterator<StorageView<T>> iterator() {
        List<StorageView<T>> views = new ArrayList<>();
        for (S part : parts) {
            for (StorageView<T> view : part) {
                views.add(view);
            }
        }
        return views.iterator();
    }
}
