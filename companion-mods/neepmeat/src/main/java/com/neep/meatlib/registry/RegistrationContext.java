/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Upstream built the block, then registered the instance. NeoForge 1.21 freezes the
 * block registry outside {@code RegisterEvent}, and {@code new Block()} writes an intrusive
 * holder, so blocks have to be constructed inside the deferred supplier.
 */
public final class RegistrationContext {
    private static final List<RegistrationContext> ALL = new ArrayList<>();

    private final String namespace;
    private final DeferredRegister.Blocks blocks;
    private final DeferredRegister.Items items;

    public RegistrationContext(String namespace) {
        this.namespace = namespace;
        this.blocks = DeferredRegister.createBlocks(namespace);
        this.items = DeferredRegister.createItems(namespace);
        ALL.add(this);
    }

    public static List<RegistrationContext> all() {
        return ALL;
    }

    public String namespace() {
        return namespace;
    }

    public void register(IEventBus bus) {
        blocks.register(bus);
        items.register(bus);
    }

    public <B extends Block> DeferredBlock<B> block(String path, Supplier<? extends B> factory) {
        return blocks.register(path, factory);
    }

    public <I extends Item> DeferredItem<I> item(String path, Supplier<? extends I> factory) {
        return items.register(path, factory);
    }
}
