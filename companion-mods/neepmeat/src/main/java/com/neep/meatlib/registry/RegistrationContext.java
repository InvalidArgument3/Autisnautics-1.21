/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Upstream built the block, then registered the instance. NeoForge 1.21 freezes the
 * block registry outside {@code RegisterEvent}, and {@code new Block()} writes an intrusive
 * holder, so blocks have to be constructed inside the deferred supplier.
 */
public final class RegistrationContext {
    private static final List<RegistrationContext> ALL = new ArrayList<>();

    private static final ThreadLocal<String> CURRENT_PATH = new ThreadLocal<>();
    private static final ThreadLocal<RegisterEvent> ACTIVE_BLOCK_EVENT = new ThreadLocal<>();

    public static void capture(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.BLOCK)) {
            ACTIVE_BLOCK_EVENT.set(event);
        }
    }

    public static void finish(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.BLOCK)) {
            ACTIVE_BLOCK_EVENT.remove();
        }
    }

    private final String namespace;
    private final DeferredRegister.Blocks blocks;
    private final DeferredRegister.Items items;
    private final Set<String> appended = new HashSet<>();

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

    public net.minecraft.world.item.CreativeModeTab getDefaultGroup() {
        return null;
    }

    public <T> T addParent(ResourceLocation id, T entry) {
        return append(null, entry, id.getPath());
    }

    public void register(IEventBus bus) {
        blocks.register(bus);
        items.register(bus);
    }

    public <B extends Block> DeferredBlock<B> block(String path, Supplier<? extends B> factory) {
        return blocks.register(path, () -> {
            String previous = CURRENT_PATH.get();
            CURRENT_PATH.set(path);
            try {
                return factory.get();
            } finally {
                if (previous == null) {
                    CURRENT_PATH.remove();
                } else {
                    CURRENT_PATH.set(previous);
                }
            }
        });
    }

    /**
     * Upstream queued the child and registered it with the parent path. NeoForge accepts
     * {@code DeferredRegister.register} during static init and while a parent supplier is running.
     */
    public <T> T append(Object parent, T entry) {
        return append(parent, entry, path -> path + "_child");
    }

    public <T> T append(Object parent, T entry, String path) {
        return append(parent, entry, ignored -> path);
    }

    public <T> T append(Object parent, T entry, Function<String, String> processor) {
        String parentPath = CURRENT_PATH.get() == null ? "block" : CURRENT_PATH.get();
        String path = processor.apply(parentPath);
        if (appended.add(path)) {
            if (entry instanceof Block block) {
                RegisterEvent event = ACTIVE_BLOCK_EVENT.get();
                if (event != null) {
                    event.register(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, path), () -> block);
                } else {
                    blocks.register(path, () -> block);
                }
            } else if (entry instanceof Item item) {
                items.register(path, () -> item);
            }
        }
        return entry;
    }

    /** Upstream registers a block item under the block's path while the block supplier is running. */
    public <B extends Block> B withItem(B block, com.neep.meatlib.item.ItemSettings settings) {
        String path = CURRENT_PATH.get() == null ? "block" : CURRENT_PATH.get();
        items.register(path, () -> settings.create(block, this));
        return block;
    }

    public <B extends Block> B withItem(B block, Item.Properties settings) {
        String path = CURRENT_PATH.get() == null ? BuiltInRegistries.BLOCK.getKey(block).getPath() : CURRENT_PATH.get();
        items.register(path, () -> new net.minecraft.world.item.BlockItem(block, settings));
        return block;
    }

    public <I extends Item> DeferredItem<I> item(String path, Supplier<? extends I> factory) {
        return items.register(path, factory);
    }
}
