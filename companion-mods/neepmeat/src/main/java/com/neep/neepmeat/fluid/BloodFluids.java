/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Upstream blood: a source, a flowing fluid, a liquid block, and a bucket.
 * Level decrease and tick rate match FluidBuilder.levelDecrease(1).tickRate(5).
 */

package com.neep.neepmeat.fluid;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.building.BuildingBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class BloodFluids {
    public static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, NeepMeat.ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, NeepMeat.ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NeepMeat.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NeepMeat.ID);

    public static final DeferredHolder<FluidType, FluidType> BLOOD_TYPE = TYPES.register("blood",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.neepmeat.blood")
                    .density(1500)
                    .viscosity(2000)
                    .canConvertToSource(false)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> BLOOD = FLUIDS.register("blood",
            () -> new BaseFlowingFluid.Source(properties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_BLOOD = FLUIDS.register("flowing_blood",
            () -> new BaseFlowingFluid.Flowing(properties()));

    public static final DeferredBlock<LiquidBlock> BLOOD_BLOCK = BLOCKS.register("blood",
            () -> new LiquidBlock(BLOOD.get(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .noCollission()
                    .strength(100.0F)
                    .noLootTable()
                    .liquid()
                    .replaceable()
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredItem<BucketItem> BLOOD_BUCKET = ITEMS.register("blood_bucket",
            () -> new BucketItem(BLOOD.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private BloodFluids() {}

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(BLOOD_TYPE, BLOOD, FLOWING_BLOOD)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(1)
                .tickRate(5)
                .block(BLOOD_BLOCK)
                .bucket(BLOOD_BUCKET);
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        FLUIDS.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        bus.addListener(BloodFluids::creative);
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == BuildingBlocks.TAB.getKey()) {
            event.accept(BLOOD_BUCKET.get());
        }
    }
}
