/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.transfer;

/**
 * Fabric's transaction participant. {@link #onRename()} is commit: the Yarn-to-Mojmap
 * rewrite renamed {@code commit} to {@code onRename} in the living-machine sources.
 */
public interface TransactionContext {
    void onRename();

    void abort();

    /** Fabric nested transaction. {@link Transaction} already opens one. */
    default TransactionContext openNested() {
        if (this instanceof Transaction transaction) {
            return transaction.openNested();
        }
        return this;
    }
}
