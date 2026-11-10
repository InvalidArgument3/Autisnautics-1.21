/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Droplets inserted are progressIncrement times 400, the upstream default.
 */

package com.neep.neepmeat.machine.live_machine.process;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.LivingMachineControllerBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.LargeCompressorBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

public class LargeCompressorProcess implements Process {
    private static final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.MOTOR_PORT,
            LivingMachineComponents.LARGE_COMPRESSOR);

    @Override
    public void serverTick(LivingMachineControllerBlockEntity machine) {
        List<LargeCompressorBlockEntity> compressors = machine.components(LivingMachineComponents.LARGE_COMPRESSOR);
        if (compressors.isEmpty() || machine.components(LivingMachineComponents.MOTOR_PORT).isEmpty()) {
            return;
        }
        long droplets = (long) (machine.getProgressIncrement() * LargeCompressorBlockEntity.POWER_TO_DROPLETS);
        compressors.getFirst().insertDroplets(droplets);
    }

    @Override
    public List<ComponentType<?>> getRequired() {
        return REQUIRED;
    }

    @Override
    public Component getName() {
        return Component.translatable("process.neepmeat.large_compressor");
    }
}
