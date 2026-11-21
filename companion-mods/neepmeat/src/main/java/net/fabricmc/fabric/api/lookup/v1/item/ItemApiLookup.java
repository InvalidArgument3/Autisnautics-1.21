/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Item API lookup shape. Providers are stored; a capability binding is the next step.
 */

package net.fabricmc.fabric.api.lookup.v1.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public final class ItemApiLookup<A, C> {
    private static final Map<ResourceLocation, ItemApiLookup<?, ?>> LOOKUPS = new ConcurrentHashMap<>();
    private final Map<ItemLike, BiFunction<ItemStack, C, A>> items = new ConcurrentHashMap<>();

    private ItemApiLookup() {}

    @SuppressWarnings("unchecked")
    public static <A, C> ItemApiLookup<A, C> get(ResourceLocation id, Class<A> api, Class<C> context) {
        return (ItemApiLookup<A, C>) LOOKUPS.computeIfAbsent(id, key -> new ItemApiLookup<>());
    }

    public void registerForItems(BiFunction<ItemStack, C, A> provider, ItemLike... items) {
        for (ItemLike item : items) {
            this.items.put(item, provider);
        }
    }

    public A find(ItemStack stack, C context) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        BiFunction<ItemStack, C, A> provider = items.get(stack.getItem());
        return provider == null ? null : provider.apply(stack, context);
    }
}
