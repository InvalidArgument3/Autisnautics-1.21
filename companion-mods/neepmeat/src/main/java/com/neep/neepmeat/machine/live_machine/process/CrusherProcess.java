/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * More than four segments does nothing. Below 0.1 power does nothing.
 * An empty segment takes the input stack, then progress increases by the increment.
 * The advanced-crushing recipe that would replace the stack is not loaded.
 */

package com.neep.neepmeat.machine.live_machine.process;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.LivingMachineControllerBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.CrusherSegmentBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.ItemPortBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CrusherProcess implements Process {
    private static final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.ITEM_INPUT,
            LivingMachineComponents.CRUSHER_SEGMENT,
            LivingMachineComponents.ITEM_OUTPUT,
            LivingMachineComponents.MOTOR_PORT);

    @Override
    public void serverTick(LivingMachineControllerBlockEntity machine) {
        List<CrusherSegmentBlockEntity> crushers = machine.components(LivingMachineComponents.CRUSHER_SEGMENT);
        List<ItemPortBlockEntity> inputs = machine.components(LivingMachineComponents.ITEM_INPUT);
        if (crushers.isEmpty() || inputs.isEmpty() || crushers.size() > 4 || machine.power() < 0.1f) {
            return;
        }
        ItemPortBlockEntity input = inputs.getFirst();
        for (CrusherSegmentBlockEntity crusher : crushers) {
            if (crusher.isEmpty() && !input.stack().isEmpty()) {
                crusher.insert(input.removeStack());
                crusher.assignCobblestoneRecipe();
            } else if (crusher.addProgress(machine.getProgressIncrement())) {
                List<ItemPortBlockEntity> outputs = machine.components(LivingMachineComponents.ITEM_OUTPUT);
                if (!outputs.isEmpty()) {
                    outputs.getFirst().setStack(crusher.takeResult());
                }
            }
        }
    }

    @Override
    public List<ComponentType<?>> getRequired() {
        return REQUIRED;
    }

    @Override
    public Component getName() {
        return Component.translatable("process.neepmeat.crusher");
    }
}
