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
 *
 * Field names and ids match upstream. Values are filled by the existing DeferredRegisters, not a second registration.
 */

package com.neep.neepmeat.init;

import net.minecraft.world.level.block.entity.BlockEntityType;

public final class NMBlockEntities {
    public static BlockEntityType<?> FLUID_PIPE;
    public static BlockEntityType<?> ENCASED_FLUID_PIPE;
    public static BlockEntityType<?> CHECK_VALVE;
    public static BlockEntityType<?> STOP_VALVE;
    public static BlockEntityType<?> FILTER_PIPE;
    public static BlockEntityType<?> LIMITER_VALVE;
    public static BlockEntityType<?> WINDOW_PIPE;
    public static BlockEntityType<?> PUMP;
    public static BlockEntityType<?> TANK;
    public static BlockEntityType<?> ADVANCED_TANK;
    public static BlockEntityType<?> FLUID_BUFFER;
    public static BlockEntityType<?> FLEX_TANK;
    public static BlockEntityType<?> ADVANCED_FLEX_TANK;
    public static BlockEntityType<?> FLEX_SILO;
    public static BlockEntityType<?> FLUID_GAUGE;
    public static BlockEntityType<?> ITEM_GAUGE;
    public static BlockEntityType<?> METAL_BARREL;
    public static BlockEntityType<?> LARGE_FAN;
    public static BlockEntityType<?> ITEM_BUFFER_BLOCK_ENTITY;
    public static BlockEntityType<?> INVENTORY_DETECTOR;
    public static BlockEntityType<?> GLASS_TANK;
    public static BlockEntityType<?> MULTI_TANK;
    public static BlockEntityType<?> HOPPER_EXTENDER;
    public static BlockEntityType<?> WOODEN_HOPPER;
    public static BlockEntityType<?> MOTOR;
    public static BlockEntityType<?> ADVANCED_MOTOR;
    public static BlockEntityType<?> LINEAR_OSCILLATOR;
    public static BlockEntityType<?> DEPLOYER;
    public static BlockEntityType<?> AGITATOR;
    public static BlockEntityType<?> VAT_ITEM_PORT;
    public static BlockEntityType<?> VAT_FLUID_PORT;
    public static BlockEntityType<?> VAT_CASING;
    public static BlockEntityType<?> VAT_WINDOW;
    public static BlockEntityType<?> VAT_CONTROLLER;
    public static BlockEntityType<?> WELL_HEAD;
    public static BlockEntityType<?> WRITHING_EARTH_SPOUT;
    public static BlockEntityType<?> PHAGE_RAY;
    public static BlockEntityType<?> FLUID_DRAIN;
    public static BlockEntityType<?> RESERVOIR;
    public static BlockEntityType<?> FLUID_INTERFACE;
    public static BlockEntityType<?> INTEGRATOR;
    public static BlockEntityType<?> ADVANCED_INTEGRATOR;
    public static BlockEntityType<?> ADVANCED_INTEGRATOR_STRUCTURE;
    public static BlockEntityType<?> HEATER;
    public static BlockEntityType<?> BIG_LEVER;
    public static BlockEntityType<?> ITEM_PIPE;
    public static BlockEntityType<?> FILTER_ITEM_PIPE;
    public static BlockEntityType<?> SPEED_ITEM_PIPE;
    public static BlockEntityType<?> OPAQUE_ITEM_PIPE;
    public static BlockEntityType<?> MERGE_ITEM_PIPE;
    public static BlockEntityType<?> BUFFER;
    public static BlockEntityType<?> CHUTE;
    public static BlockEntityType<?> EJECTOR;
    public static BlockEntityType<?> ITEM_PUMP;
    public static BlockEntityType<?> ADVANCED_EJECTOR;
    public static BlockEntityType<?> TRANSPOSER;
    public static BlockEntityType<?> ITEM_INTERFACE;
    public static BlockEntityType<?> ROUTER;
    public static BlockEntityType<?> ADVANCED_ROUTER;
    public static BlockEntityType<?> MIXER;
    public static BlockEntityType<?> CRUSHER;
    public static BlockEntityType<?> STIRLING_ENGINE;
    public static BlockEntityType<?> ALLOY_KILN;
    public static BlockEntityType<?> CRUCIBLE;
    public static BlockEntityType<?> ALEMBIC;
    public static BlockEntityType<?> TRANSDUCER;
    public static BlockEntityType<?> POWER_FLOWER_CONTROLLER;
    public static BlockEntityType<?> POWER_FLOWER_FLUID_PORT;
    public static BlockEntityType<?> VERRUCOUS_STONE_PORT;
    public static BlockEntityType<?> DUMPER;
    public static BlockEntityType<?> SMALL_TROMMEL;
    public static BlockEntityType<?> SMALL_TROMMEL_STRUCTURE;
    public static BlockEntityType<?> CASTING_BASIN;
    public static BlockEntityType<?> HYDRAULIC_PRESS;
    public static BlockEntityType<?> PEDESTAL;
    public static BlockEntityType<?> FABRICATOR;
    public static BlockEntityType<?> DEATH_BLADES;
    public static BlockEntityType<?> BOTTLER;
    public static BlockEntityType<?> FEEDING_TROUGH;
    public static BlockEntityType<?> PYLON;
    public static BlockEntityType<?> SYNTHESISER;
    public static BlockEntityType<?> MINCER;
    public static BlockEntityType<?> HOMOGENISER;
    public static BlockEntityType<?> FLAME_JET;
    public static BlockEntityType<?> ITEM_MINCER;
    public static BlockEntityType<?> FLUID_RATIONER;
    public static BlockEntityType<?> FLUID_EXCITER;
    public static BlockEntityType<?> UPGRADE_MANAGER;
    public static BlockEntityType<?> ENCASED_DATA_CABLE;
    public static BlockEntityType<?> VASCULAR_CONDUIT;
    public static BlockEntityType<?> ENCASED_VASCULAR_CONDUIT;
    public static BlockEntityType<?> VSC;
    public static BlockEntityType<?> VASCULAR_CONDENSER;
    public static BlockEntityType<?> VASCULAR_SENSOR;
    public static BlockEntityType<?> POWER_EMITTER;
    public static BlockEntityType<?> LARGE_MOTOR;
    public static BlockEntityType<?> FLYWHEEL;
    public static BlockEntityType<?> SEPARATOR;
    public static BlockEntityType<?> SMALL_COMPRESSOR;
    public static BlockEntityType<?> REACTION_CORE;
    private NMBlockEntities() {}

    /** Copies holders that are already registered elsewhere. Does not register them again. */
    public static void bind() {
        INTEGRATOR = com.neep.neepmeat.machine.StageMachines.INTEGRATOR_BE.get();
        PEDESTAL = com.neep.neepmeat.machine.StageMachines.PEDESTAL_BE.get();
        ADVANCED_INTEGRATOR = com.neep.neepmeat.machine.StageMachines.ADVANCED_INTEGRATOR_BE.get();
        MOTOR = com.neep.neepmeat.machine.NeepMachines.MOTOR_BE.get();
        ALLOY_KILN = com.neep.neepmeat.machine.NeepMachines.ALLOY_KILN_BE.get();
    }
}
