/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.transfer;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ContainerItemContext {
    static ContainerItemContext ofPlayerHand(Player player, InteractionHand hand) {
        return () -> player.getItemInHand(hand);
    }

    ItemStack getItemVariant();

    default <A> A find(Object lookup) {
        return null;
    }
}
