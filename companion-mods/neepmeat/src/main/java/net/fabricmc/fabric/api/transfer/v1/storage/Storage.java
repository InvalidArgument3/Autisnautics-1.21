/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */


package net.fabricmc.fabric.api.transfer.v1.storage;

import com.neep.meatlib.transfer.TransactionContext;

public interface Storage<T> extends Iterable<StorageView<T>> {
    long insert(T resource, long maxAmount, TransactionContext transaction);
    long extract(T resource, long maxAmount, TransactionContext transaction);
    default boolean supportsInsertion() { return true; }
    default boolean supportsExtraction() { return true; }
}
