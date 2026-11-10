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

    void spawnItem(@Nullable ItemStack stored);

    @Nullable
    default ItemStack getStored(PLC plc) {
        unsupported(plc);
        return ItemStack.EMPTY;
    }

    default void setStored(PLC plc, ItemStack stored) {
        unsupported(plc);
    }

    default void dumpStored(PLC plc) {
        spawnItem(getStored(plc));
    }

    default boolean reachedTarget(PLC plc) {
        unsupported(plc);
        return true;
    }

    default void setTarget(PLC plc, @Nullable BlockPos target) throws NeepASM.RuntimeException {
        unsupported(plc);
    }

    private void unsupported(PLC plc) {
        if (plc != null) {
            plc.raiseError(new PLC.Error("Actuator does not support action"));
        }
    }

    default double getX() { return 0; }
    default double getY() { return 0; }
    default double getZ() { return 0; }

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
