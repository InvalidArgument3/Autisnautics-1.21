/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Active waste is a solid block here. Upstream it falls.
 * Asbestos is already the building-block family cube, so this class does not register it again.
 */

package com.neep.neepmeat.block;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RecipeBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NeepMeat.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NeepMeat.ID);

    public static final DeferredBlock<Block> ACTIVE_WASTE = BLOCKS.register("active_waste",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.5F)));
    public static final DeferredItem<BlockItem> ACTIVE_WASTE_ITEM = ITEMS.register("active_waste",
            () -> new BlockItem(ACTIVE_WASTE.get(), new Item.Properties()));

    private RecipeBlocks() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
