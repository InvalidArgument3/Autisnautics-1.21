/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Reconstituted food fluid. Same shape as the other base fluids.
 */

package com.neep.neepmeat.fluid;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.building.BuildingBlocks;
import com.neep.neepmeat.init.NMFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class FoodFluid {
    public static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, NeepMeat.ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, NeepMeat.ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NeepMeat.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NeepMeat.ID);

    public static final DeferredHolder<FluidType, FluidType> TYPE = TYPES.register("food",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.neepmeat.food")
                    .density(1200)
                    .viscosity(1500)
                    .canConvertToSource(false)));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> STILL = FLUIDS.register("food",
            () -> new BaseFlowingFluid.Source(properties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING = FLUIDS.register("flowing_food",
            () -> new BaseFlowingFluid.Flowing(properties()));
    public static final DeferredBlock<LiquidBlock> BLOCK = BLOCKS.register("food",
            () -> new LiquidBlock(STILL.get(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .noCollission()
                    .strength(100.0F)
                    .noLootTable()
                    .liquid()
                    .replaceable()
                    .pushReaction(PushReaction.DESTROY)));
    public static final DeferredItem<BucketItem> BUCKET = ITEMS.register("food_bucket",
            () -> new BucketItem(STILL.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private FoodFluid() {}

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(TYPE, STILL, FLOWING)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(1)
                .tickRate(5)
                .block(BLOCK)
                .bucket(BUCKET);
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        FLUIDS.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        bus.addListener(EventPriority.LOWEST, FoodFluid::bindFields);
        bus.addListener(FoodFluid::creative);
    }

    private static void bindFields(net.neoforged.neoforge.registries.RegisterEvent event) {
        if (event.getRegistryKey() == Registries.FLUID) {
            NMFluids.FOOD = STILL.get();
            NMFluids.ETHEREAL_FUEL = EtherealFuel.STILL.get();
            NMFluids.BLOOD = BloodFluids.BLOOD.get();
            NMFluids.MEAT = MeatFluid.STILL.get();
            NMFluids.WORK_FLUID = WorkFluid.STILL.get();
            NMFluids.COMPRESSED_AIR = CompressedAir.STILL.get();
            NMFluids.TISSUE_SLURRY = TissueSlurry.STILL.get();
        }
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == BuildingBlocks.TAB.getKey()) {
            event.accept(BUCKET.get());
        }
    }
}
