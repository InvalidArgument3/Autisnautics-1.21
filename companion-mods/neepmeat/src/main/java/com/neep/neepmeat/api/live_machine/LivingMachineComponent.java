/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.live_machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public interface LivingMachineComponent {
    static @Nullable LivingMachineComponent find(Level level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        return entity instanceof LivingMachineComponent component ? component : null;
    }

    default void setController(@Nullable BlockPos pos) {}

    boolean componentRemoved();

    ComponentType<? extends LivingMachineComponent> getComponentType();

    default void exit() {
        setController(null);
    }
}
