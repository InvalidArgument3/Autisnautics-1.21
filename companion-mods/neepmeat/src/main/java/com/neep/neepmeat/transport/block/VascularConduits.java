/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.transport.block;

import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.building.BuildingBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class VascularConduits {
    private static final RegistrationContext CONTEXT = new RegistrationContext(NeepMeat.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NeepMeat.ID);

    public static final DeferredBlock<VascularConduitBlock> CONDUIT = CONTEXT.block("vascular_conduit", VascularConduitBlock::new);
    public static final DeferredItem<BlockItem> CONDUIT_ITEM = CONTEXT.item("vascular_conduit",
            () -> new BlockItem(CONDUIT.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VascularConduitBlockEntity>> CONDUIT_BE =
            BLOCK_ENTITIES.register("vascular_conduit", () -> BlockEntityType.Builder
                    .of(VascularConduitBlockEntity::new, CONDUIT.get()).build(null));

    private VascularConduits() {}

    public static void register(IEventBus bus) {
        CONTEXT.register(bus);
        BLOCK_ENTITIES.register(bus);
        NeoForge.EVENT_BUS.addListener(com.neep.neepmeat.transport.blood_network.BloodNetworkManager::onTick);
        bus.addListener(VascularConduits::creative);
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == BuildingBlocks.TAB.getKey()) {
            event.accept(CONDUIT_ITEM.get());
        }
    }
}
