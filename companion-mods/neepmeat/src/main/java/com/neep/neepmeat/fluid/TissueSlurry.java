/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Upstream tissue slurry. The trommel recipe consumes 9000 droplets of it.
 */

package com.neep.neepmeat.fluid;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class TissueSlurry {
    public static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, NeepMeat.ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, NeepMeat.ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NeepMeat.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NeepMeat.ID);

    public static final DeferredHolder<FluidType, FluidType> TYPE = TYPES.register("tissue_slurry",
            () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.neepmeat.tissue_slurry")));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> STILL = FLUIDS.register("tissue_slurry",
            () -> new BaseFlowingFluid.Source(properties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING = FLUIDS.register("flowing_tissue_slurry",
            () -> new BaseFlowingFluid.Flowing(properties()));
    public static final DeferredBlock<LiquidBlock> BLOCK = BLOCKS.register("tissue_slurry",
            () -> new LiquidBlock(STILL.get(), BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).noCollission().strength(100).liquid().replaceable().pushReaction(PushReaction.DESTROY)));
    public static final DeferredItem<BucketItem> BUCKET = ITEMS.register("tissue_slurry_bucket",
            () -> new BucketItem(STILL.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private TissueSlurry() {}

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(TYPE, STILL, FLOWING).slopeFindDistance(2).levelDecreasePerBlock(2).tickRate(5).block(BLOCK).bucket(BUCKET);
    }

    public static void bootstrap(IEventBus bus) {
        TYPES.register(bus);
        FLUIDS.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
