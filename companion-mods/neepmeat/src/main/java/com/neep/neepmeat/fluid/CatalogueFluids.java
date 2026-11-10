/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * The nine fluids upstream built with FluidBuilder. Ids, tick rate, and level
 * decrease match that registration. Sludge has no bucket, matching upstream.
 */

package com.neep.neepmeat.fluid;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.building.BuildingBlocks;
import com.neep.neepmeat.init.NMFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
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
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class CatalogueFluids {
    public static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, NeepMeat.ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, NeepMeat.ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NeepMeat.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NeepMeat.ID);

    public static final Entry PATINA_TREATMENT = entry("patina_treatment", 5, 2, true, MapColor.TERRACOTTA_ORANGE, null);
    public static final Entry ELDRITCH_ENZYMES = entry("eldritch_enzymes", 5, 2, true, MapColor.COLOR_PURPLE, null);
    public static final Entry P_MILK = entry("pasteurised_milk", 5, 1, true, MapColor.SNOW, null);
    public static final Entry ANIMAL_FEED = entry("animal_feed", 5, 4, true, MapColor.COLOR_YELLOW, SoundEvents.SAND_PLACE);
    public static final Entry PINKDRINK = entry("pinkdrink", 5, 2, true, MapColor.COLOR_PINK, null);
    public static final Entry SLUDGE = entry("sludge", 5, 2, false, MapColor.COLOR_GREEN, null);
    public static final Entry FAT = entry("fat", 5, 2, true, MapColor.COLOR_YELLOW, null);
    public static final Entry LIQUID_CLAY = entry("liquid_clay", 5, 2, true, MapColor.CLAY, null);
    public static final Entry OIL_OF_BROME = entry("oil_of_brome", 5, 2, true, MapColor.COLOR_BROWN, null);

    private CatalogueFluids() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        FLUIDS.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        bus.addListener(EventPriority.LOWEST, CatalogueFluids::bind);
        bus.addListener(CatalogueFluids::creative);
    }

    private static void bind(net.neoforged.neoforge.registries.RegisterEvent event) {
        if (event.getRegistryKey() != Registries.FLUID) {
            return;
        }
        NMFluids.PATINA_TREATMENT = PATINA_TREATMENT.still.get();
        NMFluids.ELDRITCH_ENZYMES = ELDRITCH_ENZYMES.still.get();
        NMFluids.P_MILK = P_MILK.still.get();
        NMFluids.ANIMAL_FEED = ANIMAL_FEED.still.get();
        NMFluids.PINKDRINK = PINKDRINK.still.get();
        NMFluids.SLUDGE = SLUDGE.still.get();
        NMFluids.FAT = FAT.still.get();
        NMFluids.LIQUID_CLAY = LIQUID_CLAY.still.get();
        NMFluids.OIL_OF_BROME = OIL_OF_BROME.still.get();
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != BuildingBlocks.TAB.getKey()) {
            return;
        }
        for (Entry entry : new Entry[]{PATINA_TREATMENT, ELDRITCH_ENZYMES, P_MILK, ANIMAL_FEED, PINKDRINK, FAT, LIQUID_CLAY, OIL_OF_BROME}) {
            if (entry.bucket != null) {
                event.accept(entry.bucket.get());
            }
        }
    }

    private static Entry entry(String name, int tickRate, int levelDecrease, boolean bucket, MapColor colour, net.minecraft.sounds.SoundEvent fillSound) {
        DeferredHolder<FluidType, FluidType> type = TYPES.register(name, () -> {
            FluidType.Properties properties = FluidType.Properties.create()
                    .descriptionId("fluid.neepmeat." + name)
                    .canConvertToSource(false);
            if (fillSound != null) {
                properties.sound(SoundActions.BUCKET_FILL, fillSound).sound(SoundActions.BUCKET_EMPTY, SoundEvents.SAND_BREAK);
            }
            return new FluidType(properties);
        });
        DeferredHolder<Fluid, BaseFlowingFluid.Source>[] still = holder();
        DeferredHolder<Fluid, BaseFlowingFluid.Flowing>[] flowing = holder();
        DeferredBlock<LiquidBlock> block = BLOCKS.register(name, () -> new LiquidBlock(still[0].get(), BlockBehaviour.Properties.of()
                .mapColor(colour)
                .noCollission()
                .strength(100.0F)
                .noLootTable()
                .liquid()
                .replaceable()
                .pushReaction(PushReaction.DESTROY)));
        DeferredItem<BucketItem> item = bucket ? ITEMS.register(name + "_bucket",
                () -> new BucketItem(still[0].get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))) : null;
        still[0] = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(properties(type, still[0], flowing[0], block, item, tickRate, levelDecrease)));
        flowing[0] = FLUIDS.register("flowing_" + name, () -> new BaseFlowingFluid.Flowing(properties(type, still[0], flowing[0], block, item, tickRate, levelDecrease)));
        return new Entry(still[0], flowing[0], block, item);
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] holder() {
        return (T[]) new DeferredHolder[1];
    }

    private static BaseFlowingFluid.Properties properties(
            DeferredHolder<FluidType, FluidType> type,
            DeferredHolder<Fluid, ? extends BaseFlowingFluid> still,
            DeferredHolder<Fluid, ? extends BaseFlowingFluid> flowing,
            DeferredBlock<LiquidBlock> block,
            DeferredItem<BucketItem> bucket,
            int tickRate,
            int levelDecrease) {
        BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(type, still, flowing)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(levelDecrease)
                .tickRate(tickRate)
                .block(block);
        if (bucket != null) {
            properties.bucket(bucket);
        }
        return properties;
    }

    public record Entry(
            DeferredHolder<Fluid, BaseFlowingFluid.Source> still,
            DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing,
            DeferredBlock<LiquidBlock> block,
            DeferredItem<BucketItem> bucket) {}
}
