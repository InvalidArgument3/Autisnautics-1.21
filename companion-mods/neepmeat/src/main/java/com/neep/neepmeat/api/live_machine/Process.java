/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.live_machine;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface Process {
    Component NO_PROCESS = Component.empty();

    void serverTick(LivingMachineBlockEntity machine);

    default void onStart(LivingMachineBlockEntity machine) {}

    default void onStop(LivingMachineBlockEntity machine) {}

    List<ComponentType<?>> getRequired();

    Component getName();

    default List<Component> getProblems(LivingMachineBlockEntity machine) {
        return List.of();
    }
}
