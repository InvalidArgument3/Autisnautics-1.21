/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.transfer;

import java.util.ArrayDeque;
import java.util.Deque;

/** Fabric snapshot participant. Snapshots roll back if the transaction aborts. */
public abstract class SnapshotParticipant<T> {
    private final Deque<T> snapshots = new ArrayDeque<>();

    public void updateSnapshots(TransactionContext transaction) {
        if (transaction instanceof Transaction open) {
            T snapshot = createSnapshot();
            snapshots.push(snapshot);
            open.snapshot(() -> {
                if (!snapshots.isEmpty()) {
                    readSnapshot(snapshots.pop());
                }
            });
        }
    }

    protected abstract T createSnapshot();

    protected abstract void readSnapshot(T snapshot);

    protected void onFinalCommit() {}
}
