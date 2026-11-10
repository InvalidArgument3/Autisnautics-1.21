/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Properties match upstream: max power is averaged at 300, mass is added at 100.
 */

package com.neep.neepmeat.machine.live_machine;

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.neepmeat.api.live_machine.LivingMachineStructure;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.api.live_machine.StructureProperty;
import net.minecraft.world.level.block.Block;

import java.util.EnumMap;

public class MachineBlock extends Block implements LivingMachineStructure {
    private final EnumMap<StructureProperty, PropertyValue> properties = new EnumMap<>(StructureProperty.class);

    public MachineBlock(float maxPower, float mass) {
        super(MeatlibBlockSettings.create().strength(2.0F).properties());
        properties.put(StructureProperty.MAX_POWER, new PropertyValue(maxPower));
        properties.put(StructureProperty.MASS, new PropertyValue(PropertyValue.Function.ADD, mass));
    }

    @Override
    public EnumMap<StructureProperty, PropertyValue> getProperties() {
        return properties;
    }
}
