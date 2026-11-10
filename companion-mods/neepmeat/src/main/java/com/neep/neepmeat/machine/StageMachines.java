/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine;

import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.building.BuildingBlocks;
import com.neep.neepmeat.machine.integrator.IntegratorBlock;
import com.neep.neepmeat.machine.integrator.IntegratorBlockEntity;
import com.neep.neepmeat.machine.surgery.SurgeryPlatformBlock;
import com.neep.neepmeat.machine.surgery.SurgeryPlatformBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class StageMachines {
    private static final RegistrationContext CONTEXT = new RegistrationContext(NeepMeat.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NeepMeat.ID);

    public static final DeferredBlock<IntegratorBlock> INTEGRATOR = CONTEXT.block("integrator", IntegratorBlock::new);
    public static final DeferredItem<BlockItem> INTEGRATOR_ITEM = CONTEXT.item("integrator",
            () -> new BlockItem(INTEGRATOR.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IntegratorBlockEntity>> INTEGRATOR_BE =
            BLOCK_ENTITIES.register("integrator", () -> BlockEntityType.Builder
                    .of(IntegratorBlockEntity::new, INTEGRATOR.get()).build(null));

    public static final DeferredBlock<SurgeryPlatformBlock> SURGERY = CONTEXT.block("surgery_platform", SurgeryPlatformBlock::new);
    public static final DeferredItem<BlockItem> SURGERY_ITEM = CONTEXT.item("surgery_platform",
            () -> new BlockItem(SURGERY.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SurgeryPlatformBlockEntity>> SURGERY_BE =
            BLOCK_ENTITIES.register("surgery_platform", () -> BlockEntityType.Builder
                    .of(SurgeryPlatformBlockEntity::new, SURGERY.get()).build(null));

    private StageMachines() {}

    public static void register(IEventBus bus) {
        CONTEXT.register(bus);
        BLOCK_ENTITIES.register(bus);
        bus.addListener(StageMachines::creative);
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == BuildingBlocks.TAB.getKey()) {
            event.accept(INTEGRATOR_ITEM.get());
            event.accept(SURGERY_ITEM.get());
        }
    }
}
