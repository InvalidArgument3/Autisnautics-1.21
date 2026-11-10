/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */


package net.fabricmc.fabric.api.transfer.v1.storage;

import com.neep.meatlib.transfer.TransactionContext;

public interface StorageView<T> {
    long extract(T resource, long maxAmount, TransactionContext transaction);
    boolean isResourceBlank();
    T getResource();
    long getAmount();
    long getCapacity();
}
