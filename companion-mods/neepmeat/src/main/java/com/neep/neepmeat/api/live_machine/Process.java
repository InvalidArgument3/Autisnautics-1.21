/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.live_machine;

import com.neep.neepmeat.machine.live_machine.LivingMachineControllerBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface Process {
    void serverTick(LivingMachineControllerBlockEntity machine);

    default void onStart(LivingMachineControllerBlockEntity machine) {}

    default void onStop(LivingMachineControllerBlockEntity machine) {}

    List<ComponentType<?>> getRequired();

    Component getName();

    default List<Component> getProblems(LivingMachineControllerBlockEntity machine) {
        return List.of();
    }
}
