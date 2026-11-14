/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.transfer;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PlayerInventoryStorage implements Storage<ItemVariant> {
    private final Player player;

    private PlayerInventoryStorage(Player player) {
        this.player = player;
    }

    public static PlayerInventoryStorage of(Player player) {
        return new PlayerInventoryStorage(player);
    }

    public void offerOrDrop(ItemVariant variant, long amount) {
        if (variant == null || variant.isBlank() || amount <= 0) {
            return;
        }
        player.getInventory().placeItemBackInInventory(variant.toStack((int) Math.min(amount, Integer.MAX_VALUE)));
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
    public java.util.Iterator<StorageView<ItemVariant>> iterator() {
        return java.util.List.<StorageView<ItemVariant>>of().iterator();
    }
}
