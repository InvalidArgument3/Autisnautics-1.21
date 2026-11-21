/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.transfer;

public final class StoragePreconditions {
    private StoragePreconditions() {}

    public static void notNegative(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount is negative");
        }
    }

    public static void notBlankNotNegative(TransferVariant<?> resource, long amount) {
        notNegative(amount);
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("resource is blank");
        }
    }
}
