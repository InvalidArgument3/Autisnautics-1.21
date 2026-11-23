/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.block;

import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Register with {@link #register}. The block object is created during the block registry
 * event, and the block item during the item event. Constructing either in a mod constructor
 * freezes with "Registry is already frozen".
 */
public class BaseBlock extends Block implements MeatlibBlock {
    public BaseBlock(MeatlibBlockSettings settings) {
        super(settings.properties());
    }

    /** Upstream constructed the block and registered it in one call. NeoForge registers through the context later. */
    public BaseBlock(RegistrationContext ignored, ItemSettings ignoredItems, MeatlibBlockSettings settings) {
        super(settings.properties());
    }

    public BaseBlock(RegistrationContext ignored, MeatlibBlockSettings settings) {
        super(settings.properties());
    }

    /** Loom sources pass {@link BlockBehaviour.Properties}. The item is registered by the caller. */
    public BaseBlock(RegistrationContext ignored, ItemSettings ignoredItems, net.minecraft.world.level.block.state.BlockBehaviour.Properties settings) {
        super(settings);
    }

    public BaseBlock(RegistrationContext ignored, net.minecraft.world.level.block.state.BlockBehaviour.Properties settings) {
        super(settings);
    }

    public static DeferredBlock<BaseBlock> register(RegistrationContext context, String path, MeatlibBlockSettings settings) {
        return register(context, path, settings, ItemSettings.block());
    }

    public static DeferredBlock<BaseBlock> register(RegistrationContext context, String path, MeatlibBlockSettings settings, ItemSettings items) {
        DeferredBlock<BaseBlock> block = context.block(path, () -> new BaseBlock(settings));
        context.item(path, () -> new BaseBlockItem(block.get(), items));
        return block;
    }

    @Override
    public BlockItem blockItem() {
        return (BlockItem) net.minecraft.world.item.Item.BY_BLOCK.get(this);
    }
}
