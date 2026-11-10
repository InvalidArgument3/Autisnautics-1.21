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

package com.neep.neepmeat.machine.reactor;

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.registry.annotation.RegisterMe;
import com.neep.neepmeat.NMItemGroups;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.reactor.block.ActiveWasteBlock;
import com.neep.neepmeat.machine.reactor.block.CoreSensorBlock;
import com.neep.neepmeat.machine.reactor.block.ReceiverOrganismBlock;
import com.neep.neepmeat.machine.reactor.block.entity.CoreSensorBlockEntity;
import com.neep.neepmeat.machine.reactor.block.entity.DisruptorNeedleBlockEntity;
import com.neep.neepmeat.machine.reactor.block.entity.DisruptorPortBlockEntity;
import com.neep.neepmeat.machine.reactor.block.entity.DisruptorSegmentBlockEntity;
import com.neep.neepmeat.machine.reactor.disruptor.*;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.Map;

@RegisterMe(NeepMeat.NAMESPACE)
public class IntrusionReactor
{
    public static final RegistrationContext C = new RegistrationContext(NeepMeat.NAMESPACE, () -> NMItemGroups.GENERAL);

    public static final FabricBlockSettings FLESH_SETTINGS = MeatlibBlockSettings.of().sound(SoundType.MUDDY_MANGROVE_ROOTS).properties();

    // --- Reactor ---
    public static final Block NERVOUS_TISSUE = new ReceiverOrganismBlock(C, Map.of(
            ReceiverOrganismStructure.Property.ORGANISATION, new PropertyValue(PropertyValue.Function.ADD, 10f)
    ), MeatlibBlockSettings.copyOf(FLESH_SETTINGS).properties());

    public static final Block DISRUPTOR_NEEDLE = new DisruptorNeedleBlock(C, MeatlibBlockSettings.copyOf(NMBlocks.MACHINE_SETTINGS).properties());
    public static final Block REACTION_CORE = new ReactionCoreBlock(C, MeatlibBlockSettings.copyOf(FLESH_SETTINGS).properties());
    public static final Block CORE_SENSOR = new CoreSensorBlock(C, MeatlibBlockSettings.copyOf(FLESH_SETTINGS).properties());
    public static final Block ACTIVE_WASTE = new ActiveWasteBlock(C, MeatlibBlockSettings.copyOf(FLESH_SETTINGS).properties());

    public static final Block DISRUPTOR_SEGMENT = new DisruptorSegmentBlock(C, MeatlibBlockSettings.copyOf(NMBlocks.MACHINE_SETTINGS).properties(),
            ItemSettings.block().tooltip(LivingMachineComponents.tooltip(LivingMachineComponents.DISRUPTOR_SEGMENT)));
    public static final Block DISRUPTOR_PORT = new DisruptorPortBlock(C, MeatlibBlockSettings.copyOf(NMBlocks.MACHINE_SETTINGS).properties(),
            ItemSettings.block().tooltip(LivingMachineComponents.tooltip(LivingMachineComponents.DISRUPTOR_PORT)));

    public static BlockEntityType<DisruptorSegmentBlockEntity> DISRUPTOR_SEGMENT_BE;
    public static BlockEntityType<CoreSensorBlockEntity> CORE_SENSOR_BE;
    public static BlockEntityType<DisruptorPortBlockEntity> DISRUPTOR_PORT_BE;
    public static BlockEntityType<DisruptorNeedleBlockEntity> DISRUPTOR_NEEDLE_BE;

    public static void init()
    {
        DISRUPTOR_SEGMENT_BE = NMBlockEntities.register(
            "disruptor_segment", (p, s) -> new DisruptorSegmentBlockEntity(DISRUPTOR_SEGMENT_BE, p, s), DISRUPTOR_SEGMENT);
        DISRUPTOR_PORT_BE = NMBlockEntities.register(
                "disruptor_port", (p, s) -> new DisruptorPortBlockEntity(DISRUPTOR_PORT_BE, p, s), DISRUPTOR_PORT);
        DISRUPTOR_NEEDLE_BE = NMBlockEntities.register(
                "disruptor_needle", (p, s) -> new DisruptorNeedleBlockEntity(DISRUPTOR_NEEDLE_BE, p, s), DISRUPTOR_NEEDLE);
        CORE_SENSOR_BE = NMBlockEntities.register(
                "core_sensor", (p, s) -> new CoreSensorBlockEntity(CORE_SENSOR_BE, p, s), CORE_SENSOR);

        LivingMachineComponent.LOOKUP.registerSelf(DISRUPTOR_SEGMENT_BE);
        LivingMachineComponent.LOOKUP.registerSelf(DISRUPTOR_PORT_BE);
        LivingMachineComponent.LOOKUP.registerSelf(DISRUPTOR_NEEDLE_BE);
    }
}
