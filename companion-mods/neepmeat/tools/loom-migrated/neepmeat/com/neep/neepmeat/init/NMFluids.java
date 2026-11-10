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

package com.neep.neepmeat.init;

import com.neep.meatlib.fluid.BuiltFluid;
import com.neep.meatlib.fluid.FluidBuilder;
import com.neep.neepmeat.NMItemGroups;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.processing.FluidEnegyRegistry;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.fluid.BuiltSludgeFluid;
import com.neep.neepmeat.fluid.CompressedAirFluidVariantAttributeHandler;
import com.neep.neepmeat.fluid.ReconstitutedFoodBuiltFluid;
import com.neep.neepmeat.fluid.ore_fat.OreFatFluidFactory;
import com.neep.neepmeat.item.MeatCartonStorage;
import io.github.tropheusj.milk.Milk;
import net.fabricmc.fabric.api.transfer.v1.fluid.*;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FlowingFluid;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class NMFluids
{
    public static BuiltFluid<?, ?> BLOOD;
    public static BuiltFluid<?, ?> WORK_FLUID;
    public static BuiltFluid<?, ?> PATINA_TREATMENT;
    public static BuiltFluid<?, ?> ETHEREAL_FUEL;
    public static BuiltFluid<?, ?> ELDRITCH_ENZYMES;

    public static FlowingFluid FLOWING_DIRTY_ORE_FAT;
    public static FlowingFluid STILL_DIRTY_ORE_FAT;
    public static Block DIRTY_ORE_FAT;
    public static OreFatFluidFactory DIRTY_ORE_FAT_FACTORY = new OreFatFluidFactory(NeepMeat.NAMESPACE, "ore_fat", false, 5, 2);

    public static FlowingFluid FLOWING_CLEAN_ORE_FAT;
    public static FlowingFluid STILL_CLEAN_ORE_FAT;
    public static Block CLEAN_ORE_FAT;
    public static OreFatFluidFactory CLEAN_ORE_FAT_FACTORY = new OreFatFluidFactory(NeepMeat.NAMESPACE, "clean_ore_fat", false, 5, 2);

    public static BuiltFluid<?, ?> P_MILK;
    public static BuiltFluid<?, ?> ANIMAL_FEED;
    public static BuiltFluid<?, ?> PINKDRINK;
    public static BuiltFluid<?, ?> COMPRESSED_AIR;
    public static BuiltFluid<?, ?> SLUDGE;
    public static BuiltFluid<?, ?> FAT;
    public static BuiltFluid<?, ?> LIQUID_CLAY;
    public static BuiltFluid<?, ?> FOOD;
    public static BuiltFluid<?, ?> MEAT;
    public static BuiltFluid<?, ?> TISSUE_SLURRY;
    public static BuiltFluid<?, ?> OIL_OF_BROME;

    public static void initialise()
    {
        BLOOD = new FluidBuilder(NeepMeat.NAMESPACE, "blood").withBlock().withItem(NMItemGroups.GENERAL).levelDecrease(1).tickRate(5).build();
        WORK_FLUID = new FluidBuilder(NeepMeat.NAMESPACE, "work_fluid").withBlock().withItem(NMItemGroups.GENERAL).levelDecrease(1).tickRate(5).build();
        PATINA_TREATMENT = new FluidBuilder(NeepMeat.NAMESPACE, "patina_treatment").withBlock().withItem(NMItemGroups.GENERAL).tickRate(5).levelDecrease(2).build();
        ETHEREAL_FUEL = new FluidBuilder(NeepMeat.NAMESPACE, "ethereal_fuel").withBlock().withItem(NMItemGroups.GENERAL).tickRate(5).levelDecrease(2).build();
        ELDRITCH_ENZYMES = new FluidBuilder(NeepMeat.NAMESPACE, "eldritch_enzymes").withBlock().withItem(NMItemGroups.GENERAL).tickRate(5).levelDecrease(2).build();

        STILL_DIRTY_ORE_FAT = DIRTY_ORE_FAT_FACTORY.registerStill();
        FLOWING_DIRTY_ORE_FAT = DIRTY_ORE_FAT_FACTORY.registerFlowing();
        DIRTY_ORE_FAT = DIRTY_ORE_FAT_FACTORY.registerBlock();

        STILL_CLEAN_ORE_FAT = CLEAN_ORE_FAT_FACTORY.registerStill();
        FLOWING_CLEAN_ORE_FAT = CLEAN_ORE_FAT_FACTORY.registerFlowing();
        CLEAN_ORE_FAT = CLEAN_ORE_FAT_FACTORY.registerBlock();

        FOOD = new FluidBuilder(NeepMeat.NAMESPACE, "coarse_meat").fluidFactory(ReconstitutedFoodBuiltFluid::new).withBlock().build();

        MEAT = new FluidBuilder(NeepMeat.NAMESPACE, "meat").withBlock().withItem(NMItemGroups.GENERAL).isInfinite(false).tickRate(5).levelDecrease(2).build();
        TISSUE_SLURRY = new FluidBuilder(NeepMeat.NAMESPACE, "tissue_slurry").withBlock().withItem(NMItemGroups.GENERAL).levelDecrease(2).tickRate(5).build();

//        STILL_MILK = MILK_FACTORY.registerStill();
//        FLOWING_MILK = MILK_FACTORY.registerFlowing();
//        MILK = MILK_FACTORY.registerBlock();

        P_MILK = new FluidBuilder(NeepMeat.NAMESPACE, "pasteurised_milk").withBlock().withItem(NMItemGroups.GENERAL).tickRate(5).levelDecrease(1).build();

        ANIMAL_FEED = new FluidBuilder(NeepMeat.NAMESPACE, "animal_feed")
                .tickRate(5).levelDecrease(4).fillSound(SoundEvents.SAND_PLACE)
                .withBlock().withItem(NMItemGroups.GENERAL).build();
        FluidVariantAttributes.register(
                ANIMAL_FEED.still(), new FluidVariantAttributeHandler()
                {
                    @Override
                    public Optional<SoundEvent> getEmptySound(FluidVariant variant)
                    {
                        return Optional.of(SoundEvents.SAND_BREAK);
                    }
                });

                PINKDRINK = new FluidBuilder(NeepMeat.NAMESPACE, "pinkdrink").withItem(NMItemGroups.GENERAL).withBlock().build();

        SLUDGE = new FluidBuilder(NeepMeat.NAMESPACE, "sludge").withBlock().fluidFactory(BuiltSludgeFluid::new).build();
        FAT = new FluidBuilder(NeepMeat.NAMESPACE, "fat").withBlock().withItem(NMItemGroups.GENERAL).build();
        LIQUID_CLAY = new FluidBuilder(NeepMeat.NAMESPACE, "liquid_clay").withBlock().withItem(NMItemGroups.GENERAL).build();

        COMPRESSED_AIR = new FluidBuilder(NeepMeat.NAMESPACE, "compressed_air").withItem(NMItemGroups.GENERAL).withBlock().build();
        FluidVariantAttributes.register(COMPRESSED_AIR.still(), new CompressedAirFluidVariantAttributeHandler());

        OIL_OF_BROME = new FluidBuilder(NeepMeat.NAMESPACE, "oil_of_brome").withItem(NMItemGroups.GENERAL).withBlock().build();

        FluidEnegyRegistry.getInstance().register(ETHEREAL_FUEL.still(), 2 * PowerUtil.DROPLET_POWER, true, null);

        FluidStorage.combinedItemApiProvider(NMItems.PINKDRINK).register(context ->
                new FullItemFluidStorage(context, Items.GLASS_BOTTLE, PINKDRINK.variant(), FluidConstants.BOTTLE));
        FluidStorage.combinedItemApiProvider(Items.GLASS_BOTTLE).register(context ->
                new EmptyItemFluidStorage(context, NMItems.PINKDRINK, PINKDRINK.still(), FluidConstants.BOTTLE));

        FluidStorage.combinedItemApiProvider(NMItems.MILK_CARTON).register(context ->
                new FullItemFluidStorage(context, NMItems.CARTON, FluidVariant.of(NMFluids.P_MILK.still()), FluidConstants.BOTTLE));
        FluidStorage.combinedItemApiProvider(NMItems.CARTON).register(context ->
                new EmptyItemFluidStorage(context, NMItems.MILK_CARTON, NMFluids.P_MILK.still(), FluidConstants.BOTTLE));

//        FluidStorage.combinedItemApiProvider(NMItems.MILK_CARTON).register(context ->
//                new FullItemFluidStorage(context, NMItems.CARTON, FluidVariant.of(NMFluids.STILL_P_MILK), FluidConstants.BOTTLE));
        FluidStorage.combinedItemApiProvider(NMItems.CARTON).register(context ->
                new MeatCartonStorage(context, NMItems.CARTON, NMItems.FOOD_CARTON, FOOD.still(), FluidConstants.INGOT));
        FluidStorage.combinedItemApiProvider(NMItems.CARTON).register(context ->
                new MeatCartonStorage(context, NMItems.CARTON, NMItems.MEAT_CARTON, MEAT.still(), FluidConstants.INGOT));

//        FluidStorage.combinedItemApiProvider(Items.MILK_BUCKET).register(context ->
//                new FullItemFluidStorage(context, Items.BUCKET, FluidVariant.of(Milk.STILL_MILK), FluidConstants.BUCKET));
//        FluidStorage.combinedItemApiProvider(Items.BUCKET).register(context ->
//                new EmptyItemFluidStorage(context, Items.MILK_BUCKET, Milk.STILL_MILK, FluidConstants.BUCKET));

        Milk.enableMilkFluid();
    }

    public static boolean isGas(FluidVariant fluidVariant)
    {
        return fluidVariant.isOf(COMPRESSED_AIR.still());
    }
}