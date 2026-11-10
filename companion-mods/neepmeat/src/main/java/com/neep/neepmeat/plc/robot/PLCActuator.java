/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.robot;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.api.plc.PLC;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Upstream stored a Fabric {@code ResourceAmount<ItemVariant>}. One {@link ItemStack}
 * is the same pair of item and count.
 */
public interface PLCActuator {
    BlockPos getBasePos();

    void spawnItem(ItemStack stored);

    ItemStack getStored(PLC plc);

    void setStored(PLC plc, ItemStack stored);

    void dumpStored(PLC plc);

    boolean reachedTarget(PLC plc);

    void setTarget(PLC plc, @Nullable BlockPos target) throws NeepASM.RuntimeException;

    double getX();

    double getY();

    double getZ();

    boolean actuatorRemoved();

    default EnumSet<Capability> capabilities() {
        return EnumSet.noneOf(Capability.class);
    }

    interface Provider {
        PLCActuator getPlcActuator();
    }

    enum Capability {
        MOVE_ROBOT,
        ROUTE_ITEM
    }
}
