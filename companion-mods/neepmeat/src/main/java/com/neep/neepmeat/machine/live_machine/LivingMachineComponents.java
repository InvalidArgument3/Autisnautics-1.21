/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Bit indices follow upstream registration order. Only the motor port has a block so far.
 */

package com.neep.neepmeat.machine.live_machine;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;

public final class LivingMachineComponents {
    public static final ComponentType<LivingMachineComponent> CRUSHER_SEGMENT = type();
    public static final ComponentType<LivingMachineComponent> REPROCESSOR = type();
    public static final ComponentType<LivingMachineComponent> LARGE_TROMMEL = type();
    public static final ComponentType<LivingMachineComponent> MOTOR_PORT = type();
    public static final ComponentType<LivingMachineComponent> INTEGRATION_PORT = type();
    public static final ComponentType<LivingMachineComponent> SERVICE_PORT = type();
    public static final ComponentType<LivingMachineComponent> PHAGE_RAY = type();
    public static final ComponentType<LivingMachineComponent> TREE_VACUUM = type();
    public static final ComponentType<LivingMachineComponent> CHARNEL_PUMP = type();
    public static final ComponentType<LivingMachineComponent> LARGE_COMPRESSOR = type();
    public static final ComponentType<LivingMachineComponent> STOMACH = type();
    public static final ComponentType<LivingMachineComponent> METABOLISER_SEGMENT = type();
    public static final ComponentType<LivingMachineComponent> BALL_MILL = type();
    public static final ComponentType<LivingMachineComponent> HOPPER = type();
    public static final ComponentType<LivingMachineComponent> ITEM_INPUT = type();
    public static final ComponentType<LivingMachineComponent> ITEM_OUTPUT = type();
    public static final ComponentType<LivingMachineComponent> FLUID_INPUT = type();
    public static final ComponentType<LivingMachineComponent> FLUID_OUTPUT = type();
    public static final ComponentType<LivingMachineComponent> EXTRACTOR = type();
    public static final ComponentType<LivingMachineComponent> SILKY_EXTRACTOR = type();
    public static final ComponentType<LivingMachineComponent> DISTRIBUTOR_POINT = type();
    public static final ComponentType<LivingMachineComponent> LUCKY_ONE = type();
    public static final ComponentType<LivingMachineComponent> DISRUPTOR_SEGMENT = type();
    public static final ComponentType<LivingMachineComponent> DISRUPTOR_PORT = type();
    public static final ComponentType<LivingMachineComponent> DISRUPTOR_NEEDLE = type();

    private LivingMachineComponents() {}

    private static ComponentType<LivingMachineComponent> type() {
        ComponentType.Simple<LivingMachineComponent> created = new ComponentType.Simple<>();
        ComponentType.ID_TO_TYPE.add(created);
        return created;
    }
}
