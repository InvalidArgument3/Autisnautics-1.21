/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.transfer;

public abstract class SingleVariantItemStorage extends SnapshotParticipant<ItemVariant> implements SingleSlotStorage<ItemVariant> {
    protected ItemVariant variant = ItemVariant.BLANK;
    protected long amount;

    @Override
    public ItemVariant getResource() {
        return variant;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public boolean isResourceBlank() {
        return variant == null || variant.isBlank();
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    protected ItemVariant createSnapshot() {
        return variant;
    }

    @Override
    protected void readSnapshot(ItemVariant snapshot) {
        this.variant = snapshot;
    }

    @Override
    public java.util.Iterator<StorageView<ItemVariant>> iterator() {
        return java.util.List.<StorageView<ItemVariant>>of(this).iterator();
    }
}
