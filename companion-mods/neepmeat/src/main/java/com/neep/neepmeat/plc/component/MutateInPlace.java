/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.component;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Workpiece mutation lands with manufacture. Until then a lookup finds nothing,
 * and INJECT returns the fluid it picked up.
 */
public interface MutateInPlace<T> {
    Lookup<ItemStack> ITEM = (level, pos, direction) -> null;
    Lookup<Entity> ENTITY = (level, pos, direction) -> null;

    @FunctionalInterface
    interface Lookup<T> {
        @Nullable MutateInPlace<T> find(Level level, BlockPos pos, @Nullable Direction direction);
    }
}
