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
import com.neep.neepmeat.machine.reactor.block.entity.DisruptorPortBlockEntity;
import com.neep.neepmeat.machine.reactor.block.entity.ReactionCoreBlockEntity;
import com.neep.neepmeat.machine.reactor.disruptor.DisruptorNeedle;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class DisruptorProcess implements Process
{
    private static final List<ComponentType<?>> REQUIRED = List.of(LivingMachineComponents.DISRUPTOR_SEGMENT, LivingMachineComponents.DISRUPTOR_PORT, LivingMachineComponents.DISRUPTOR_NEEDLE);

    @Override
    public void serverTick(LivingMachineBlockEntity be)
    {
        be.withComponents(LivingMachineComponents.DISRUPTOR_SEGMENT, LivingMachineComponents.DISRUPTOR_PORT, LivingMachineComponents.DISRUPTOR_NEEDLE).ifPresent(r ->
        {
            var segments = r.t1();
            DisruptorPortBlockEntity port = r.t2().iterator().next();
            var needles = r.t3();

            Level world = be.getLevel();
            if (world.getGameTime() % 20 == 0)
            {
                port.setCore(null);
                ReactionCoreBlockEntity core = updateOrganism(world, needles);
                if (core != null)
                {
                    port.setCore(core);
                }
            }

            int disruption = port.getDisruption();

            // Update segments (visual changes only)
            if (world.getGameTime() % 3 == 0)
            {
                for (var segment : segments)
                {
                    segment.setDisruption(disruption);
                }
            }

            // There's currently no way of detecting when a process deactivates.
            // It's possible for an old core to still be references by this port.
            // Bear this in mind when making changes.
            ReactionCoreBlockEntity core = port.getCore();
            if (core != null)
            {
                // This should be safe to call on a removed core.
                float maxIncrease = be.getPower() * 100;
                core.setDisruptorParams(disruption, maxIncrease);
            }
        });
    }

    @Override
    public void onStart(LivingMachineBlockEntity be)
    {
        be.withComponents(LivingMachineComponents.DISRUPTOR_NEEDLE).ifPresent(r ->
        {
            r.t1().forEach(needle -> needle.updateState(true));
        });
    }

    @Override
    public void onStop(LivingMachineBlockEntity be)
    {
        be.withComponents(LivingMachineComponents.DISRUPTOR_PORT, LivingMachineComponents.DISRUPTOR_NEEDLE).ifPresent(r ->
        {
            r.t1().iterator().next().setCore(null);
            r.t2().forEach(needle -> needle.updateState(false));
        });
    }

    @Nullable
    private ReactionCoreBlockEntity updateOrganism(Level world, Collection<DisruptorNeedle> needles)
    {
        for (var needle : needles)
        {
            ReactionCoreBlockEntity found = needle.findCore();
            if (found != null)
                return found;
        }
        return null;
    }

    @Override
    public List<ComponentType<?>> getRequired()
    {
        return REQUIRED;
    }

    @Override
    public Component getName()
    {
        return NeepMeat.translationKey("process", "disruptor");
    }
}
