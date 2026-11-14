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

import com.neep.meatlib.fluid.BuiltFluid;
import net.minecraft.world.level.material.Fluid;

public final class NMFluids {
    public static Fluid ETHEREAL_FUEL;
    public static Fluid FOOD;
    public static Fluid FLOWING_DIRTY_ORE_FAT;
    public static Fluid STILL_DIRTY_ORE_FAT;
    public static Fluid DIRTY_ORE_FAT;
    public static Fluid DIRTY_ORE_FAT_FACTORY;
    public static Fluid FLOWING_CLEAN_ORE_FAT;
    public static Fluid STILL_CLEAN_ORE_FAT;
    public static Fluid CLEAN_ORE_FAT;
    public static Fluid CLEAN_ORE_FAT_FACTORY;
    private NMFluids() {}
    public static BuiltFluid<?, ?> ANIMAL_FEED;
    public static net.minecraft.world.level.material.Fluid BLOOD;
    public static net.minecraft.world.level.material.Fluid COMPRESSED_AIR;
    public static BuiltFluid<?, ?> ELDRITCH_ENZYMES;
    public static BuiltFluid<?, ?> FAT;
    public static BuiltFluid<?, ?> LIQUID_CLAY;
    public static net.minecraft.world.level.material.Fluid MEAT;
    public static BuiltFluid<?, ?> OIL_OF_BROME;
    public static BuiltFluid<?, ?> PATINA_TREATMENT;
    public static BuiltFluid<?, ?> PINKDRINK;
    public static BuiltFluid<?, ?> P_MILK;
    public static BuiltFluid<?, ?> SLUDGE;
    public static net.minecraft.world.level.material.Fluid TISSUE_SLURRY;
    public static net.minecraft.world.level.material.Fluid WORK_FLUID;
}
