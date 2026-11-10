/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Each segment asks for 4 droplets. Meat yields 40 energy per droplet and is consumed 1:1.
 * The produced energy is multiplied by 1.5 and split across the segments.
 * More than 6 segments produces nothing.
 */

package com.neep.neepmeat.machine.live_machine.process;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.fluid.MeatFluid;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.MetaboliserSegmentBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.StomachBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MetaboliserProcess implements Process {
    public static final int MAX_SEGMENTS = 6;
    private static final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.STOMACH,
            LivingMachineComponents.METABOLISER_SEGMENT);

    @Override
    public void serverTick(LivingMachineBlockEntity machine) {
        List<StomachBlockEntity> stomachs = machine.components(LivingMachineComponents.STOMACH);
        List<MetaboliserSegmentBlockEntity> segments = machine.components(LivingMachineComponents.METABOLISER_SEGMENT);
        if (stomachs.isEmpty() || segments.isEmpty() || segments.size() > MAX_SEGMENTS) {
            return;
        }
        long toConsume = segments.size() * 4L;
        long energyProduced = 0;
        for (StomachBlockEntity stomach : stomachs) {
            if (toConsume <= 0 || stomach.fluid() != MeatFluid.STILL.get()) {
                continue;
            }
            long extracted = stomach.extractDroplets(toConsume);
            toConsume -= extracted;
            energyProduced += 40L * extracted;
        }
        long each = (long) ((energyProduced * 1.5) / segments.size());
        for (MetaboliserSegmentBlockEntity segment : segments) {
            segment.setOutputEnergy(each);
        }
    }

    @Override
    public List<Component> getProblems(LivingMachineBlockEntity machine) {
        if (machine.components(LivingMachineComponents.METABOLISER_SEGMENT).size() > MAX_SEGMENTS) {
            return List.of(Component.literal("Too many segments (>" + MAX_SEGMENTS + ")"));
        }
        return List.of();
    }

    @Override
    public List<ComponentType<?>> getRequired() {
        return REQUIRED;
    }

    @Override
    public Component getName() {
        return Component.translatable("process.neepmeat.metaboliser");
    }
}
