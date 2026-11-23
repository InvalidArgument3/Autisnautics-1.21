/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.transfer;

public interface InsertionOnlyStorage<T> extends Storage<T> {
    @Override
    default long extract(T resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

}
