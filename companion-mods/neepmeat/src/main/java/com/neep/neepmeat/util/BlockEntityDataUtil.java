/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

/**
 * {@code BlockItem.getBlockEntityData(ItemStack)} was removed in 1.21.
 * The tag now lives on {@link DataComponents#BLOCK_ENTITY_DATA}.
 */
public final class BlockEntityDataUtil {
    private BlockEntityDataUtil() {}

    public static @Nullable CompoundTag get(ItemStack stack) {
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        return data == null ? null : data.copyTag();
    }
}
