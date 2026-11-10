/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.machine.live_machine.process;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.reactor.block.entity.DisruptorNeedleBlockEntity;
import com.neep.neepmeat.machine.reactor.block.entity.DisruptorPortBlockEntity;
import com.neep.neepmeat.machine.reactor.block.entity.DisruptorSegmentBlockEntity;
import com.neep.neepmeat.machine.reactor.block.entity.ReactionCoreBlockEntity;
import com.neep.neepmeat.machine.reactor.disruptor.DisruptorNeedle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class DisruptorProcess implements Process {
    private static final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.DISRUPTOR_SEGMENT,
            LivingMachineComponents.DISRUPTOR_PORT,
            LivingMachineComponents.DISRUPTOR_NEEDLE);

    @Override
    public void serverTick(LivingMachineBlockEntity be) {
        be.withComponents(LivingMachineComponents.DISRUPTOR_SEGMENT, LivingMachineComponents.DISRUPTOR_PORT, LivingMachineComponents.DISRUPTOR_NEEDLE).ifPresent(result -> {
            Collection<DisruptorSegmentBlockEntity> segments = cast(result.t1());
            DisruptorPortBlockEntity port = (DisruptorPortBlockEntity) result.t2().iterator().next();
            Collection<DisruptorNeedle> needles = cast(result.t3());
            Level world = be.getLevel();
            if (world != null && world.getGameTime() % 20 == 0) {
                port.setCore(null);
                ReactionCoreBlockEntity core = updateOrganism(needles);
                if (core != null) {
                    port.setCore(core);
                }
            }
            int disruption = port.getDisruption();
            if (world != null && world.getGameTime() % 3 == 0) {
                for (DisruptorSegmentBlockEntity segment : segments) {
                    segment.setDisruption(disruption);
                }
            }
            ReactionCoreBlockEntity core = port.getCore();
            if (core != null) {
                core.setDisruptorParams(disruption, be.getPower() * 100f);
            }
        });
    }

    @Override
    public void onStart(LivingMachineBlockEntity be) {
        be.withComponents(LivingMachineComponents.DISRUPTOR_NEEDLE).ifPresent(result -> {
            for (DisruptorNeedle needle : this.<DisruptorNeedle>cast(result.t1())) {
                needle.updateState(true);
            }
        });
    }

    @Override
    public void onStop(LivingMachineBlockEntity be) {
        be.withComponents(LivingMachineComponents.DISRUPTOR_PORT, LivingMachineComponents.DISRUPTOR_NEEDLE).ifPresent(result -> {
            ((DisruptorPortBlockEntity) result.t1().iterator().next()).setCore(null);
            for (DisruptorNeedle needle : this.<DisruptorNeedle>cast(result.t2())) {
                needle.updateState(false);
            }
        });
    }

    @Nullable
    private ReactionCoreBlockEntity updateOrganism(Collection<DisruptorNeedle> needles) {
        for (DisruptorNeedle needle : needles) {
            ReactionCoreBlockEntity found = needle.findCore();
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> Collection<T> cast(Collection<?> values) {
        return (Collection<T>) values;
    }

    @Override
    public List<ComponentType<?>> getRequired() {
        return REQUIRED;
    }

    @Override
    public Component getName() {
        return NeepMeat.translationKey("process", "disruptor");
    }
}
