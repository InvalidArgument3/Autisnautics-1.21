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

import net.minecraft.world.level.block.Block;

public final class NMBlocks {
    /** Loom registration uses {@code NMBlocks.block()} for the item settings. */
    public static com.neep.meatlib.item.ItemSettings block() {
        return com.neep.meatlib.item.ItemSettings.block();
    }
    public static Block C;
    public static Block METAL;
    public static Block BRASS_BLOCKS;
    public static Block FLUID_PIPE_SETTINGS;
    public static Block VASCULAR_CONDUIT_SETTINGS;
    public static Block MACHINE_SETTINGS;
    public static Block OPAQUE_MACHINE_SETTINGS;
    public static Block VAT_SETTINGS;
    public static Block FLUID_MACHINE_SETTINGS;
    public static Block ITEM_PIPE_SETTINGS;
    public static Block DUAT_STONE;
    public static Block DUAT_COBBLESTONE;
    public static Block DUAT_GRAVEL;
    public static Block OBJ_TEST;
    public static Block TEST_MULTIBLOCK;
    public static Block SMOOTH_TILE;
    public static Block POLISHED_IRON_BRICKS;
    public static Block POLISHED_MERAL_SMALL_BRICKS;
    public static Block POLISHED_METAL;
    public static Block BLUE_IRON_BLOCK;
    public static Block RUSTY_METAL_BLOCK;
    public static Block RUSTY_METAL_SHEET;
    public static Block RUSTY_METAL_DOOR;
    public static Block RUSTY_METAL_TRAPDOOR;
    public static Block RUSTY_METAL_SHEET_TRAPDOOR;
    public static Block RUSTY_METAL_LIGHT;
    public static Block WHITE_ROUGH_CONCRETE;
    public static Block GREY_ROUGH_CONCRETE;
    public static Block YELLOW_ROUGH_CONCRETE;
    public static Block RED_ROUGH_CONCRETE;
    public static Block BLUE_ROUGH_CONCRETE;
    public static Block GREEN_ROUGH_CONCRETE;
    public static Block YELLOW_TILES;
    public static Block CAUTION_BLOCK;
    public static Block CAUTION_BLOCK_DOOR;
    public static Block DIRTY_RED_TILES;
    public static Block DIRTY_WHITE_TILES;
    public static Block SANDY_BRICKS;
    public static Block MEAT_STEEL_BLOCK;
    public static Block ASBESTOS;
    public static Block ASBESTOS_TILE;
    public static Block CORRUGATED_ASBESTOS;
    public static Block PAINTED_CORRUGATED_ASBESTOS;
    public static Block ASBESTOS_SHINGLE;
    public static Block BLOODY_BRICKS;
    public static Block BLOODY_TILES;
    public static Block REINFORCED_GLASS;
    public static Block FILLED_SCAFFOLD;
    public static Block SCAFFOLD_PLATFORM;
    public static Block BLUE_SCAFFOLD;
    public static Block YELLOW_SCAFFOLD;
    public static Block RUSTY_VENT;
    public static Block MESH_PANE;
    public static Block RUSTED_BARS;
    public static Block RUSTY_PANEL;
    public static Block RUSTY_GRATE;
    public static Block DIRTY_SINK;
    public static Block SMALL_SINK;
    public static Block TELEVISION_OFF;
    public static Block TELEVISION_STATIC;
    public static Block LARGE_FAN;
    public static Block CEILING_LIGHT;
    public static Block SLOPE_TEST;
    public static Block SCAFFOLD_TRAPDOOR;
    public static Block RUSTY_METAL_LADDER;
    public static Block RUSTY_METAL_RUNGS;
    public static Block CAUTION_TAPE;
    public static Block TROMMEL;
    public static Block TROMMEL_STRUCTURE;
    public static Block SMALL_TROMMEL;
    public static Block SMALL_TROMMEL_STRUCTURE;
    public static Block CENTRIFUGE;
    public static Block HEATER;
    public static Block LINEAR_OSCILLATOR;
    public static Block MOTOR;
    public static Block ADVANCED_MOTOR;
    public static Block LARGE_MOTOR;
    public static Block FLYWHEEL;
    public static Block STIRLING_ENGINE;
    public static Block DEPLOYER;
    public static Block AGITATOR;
    public static Block CRUSHER;
    public static Block ALLOY_KILN;
    public static Block CRUCIBLE;
    public static Block COLLECTOR;
    public static Block ASSEMBLER;
    public static Block FABRICATOR;
    public static Block FLUID_EXCITER;
    public static Block TRANSDUCER;
    public static Block POWER_FLOWER_SETTINGS;
    public static Block POWER_FLOWER_SEEDS;
    public static Block POWER_FLOWER_GROWTH;
    public static Block POWER_FLOWER_CONTROLLER;
    public static Block POWER_FLOWER_FLUID_PORT;
    public static Block VERRUCOUS_STONE;
    public static Block VERRUCOUS_STONE_PORT;
    public static Block PEDESTAL;
    public static Block CHARNEL_COMPACTOR;
    public static Block CHARNEL_PUMP;
    public static Block WELL_HEAD;
    public static Block CONTAMINATED_DIRT;
    public static Block CONTAMINATED_GRASS_BLOCK;
    public static Block CONTAMINATED_RUBBLE;
    public static Block WRITHING_EARTH_SPOUT;
    public static Block WRITHING_STONE;
    public static Block PHAGE_RAY;
    public static Block MIXER;
    public static Block CASTING_BASIN;
    public static Block HYDRAULIC_PRESS;
    public static Block WORKSTATION;
    public static Block DEATH_BLADES;
    public static Block BROWNE_ORE;
    public static Block DEEPSLATE_BROWNE_ORE;
    public static Block BROWNE_BLOCK;
    public static Block GRAVE_BROME;
    public static Block LONG_BROME;
    public static Block FEEDING_TROUGH;
    public static Block SEPARATOR;
    public static Block SIFTER;
    public static Block SIFTER_HOPPER;
    public static Block PYLON;
    public static Block SYNTHESISER;
    public static Block MINCER;
    public static Block ITEM_MINCER;
    public static Block HOMOGENISER;
    public static Block SURGERY_PLATFORM;
    public static Block UPGRADE_MANAGER;
    public static Block VAT_CASING;
    public static Block VAT_CONTROLLER;
    public static Block VAT_ITEM_PORT;
    public static Block VAT_FLUID_PORT;
    public static Block VAT_WINDOW;
    public static Block FLAME_JET;
    public static Block ITEM_BUFFER;
    public static Block SPIGOT;
    public static Block BOTTLER;
    public static Block SMALL_COMPRESSOR;
    public static Block CHUTE;
    public static Block METAL_BARREL;
    public static Block CONTENT_DETECTOR;
    public static Block SOLIDITY_DETECTOR;
    public static Block DATA_CABLE;
    public static Block ENCASED_DATA_CABLE;
    public static Block VASCULAR_CONDUIT;
    public static Block VASCULAR_CONDENSER;
    public static Block VASCULAR_SENSOR;
    public static Block ENCASED_VASCULAR_CONDUIT;
    public static Block POWER_EMITTER;
    public static Block VSC;
    public static Block WHISPER_WHEAT;
    public static Block FLESH_POTATO;
    public static Block ROCKWART;
    public static Block BLOOD_BUBBLE_SAPLING;
    public static Block BB_SETTINGS;
    public static Block BLOOD_BUBBLE_LOG;
    public static Block BLOOD_BUBBLE_WOOD;
    public static Block BLOOD_BUBBLE_LEAVES;
    public static Block BLOOD_BUBBLE_LEAVES_FLOWERING;
    public static Block BLOOD_BUBBLE_PLANKS;
    public static Block BLOOD_BUBBLE_TRAPDOOR;
    public static Block BLOOD_BUBBLE_DOOR;
    public static Block BLOOD_BUBBLE_BUTTON;
    public static Block BLOOD_BUBBLE_FENCE_GATE;
    public static Block BLOOD_BUBBLE_PRESSURE_PLATE;
    public static Block LINEAR_RAIL;
    public static Block BIG_LEVER;
    public static Block INTEGRATOR_EGG;
    public static Block ADVANCED_INTEGRATOR_EGG;
    public static Block ADVANCED_INTEGRATOR;
    public static Block ADVANCED_INTEGRATOR_STRUCTURE;
    public static Block STATUE;
    public static Block DIRTY_TOILET;
    public static Block CRONENCAKE;
    public static Block CANDLE_CRONENCAKE;
    public static Block ROUTE_TEST;
    public static Block HOLDING_TRACK;
    public static Block DUMPING_TRACK;
    public static Block CONTROL_TRACK;
    public static Block ABOMINATION;
    private NMBlocks() {}
}
