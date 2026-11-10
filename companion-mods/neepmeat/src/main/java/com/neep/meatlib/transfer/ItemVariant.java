/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.transfer;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record ItemVariant(Item item, DataComponentPatch components) implements TransferVariant<Item> {
    public static final ItemVariant BLANK = new ItemVariant(Items.AIR, DataComponentPatch.EMPTY);

    public static ItemVariant of(Item item) {
        return item == null || item == Items.AIR ? BLANK : new ItemVariant(item, DataComponentPatch.EMPTY);
    }

    public static ItemVariant of(ItemStack stack) {
        return stack.isEmpty() ? BLANK : new ItemVariant(stack.getItem(), stack.getComponentsPatch());
    }

    public boolean isBlank() {
        return item == null || item == Items.AIR;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public Item getObject() {
        return item;
    }

    /**
     * Network form of this variant. Uses {@link ItemStack#OPTIONAL_STREAM_CODEC} when the
     * buffer carries registries, and {@link ItemStack#OPTIONAL_CODEC} otherwise. Both keep
     * the {@link DataComponentPatch}.
     */
    public static final com.neep.meatlib.api.network.ParamCodec<ItemVariant> PARAM_CODEC = com.neep.meatlib.api.network.ParamCodec.of(
            ItemVariant.class,
            (value, buf) -> {
                ItemStack stack = value == null ? ItemStack.EMPTY : value.toStack();
                if (buf instanceof net.minecraft.network.RegistryFriendlyByteBuf registry) {
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(registry, stack);
                } else {
                    buf.writeJsonWithCodec(ItemStack.OPTIONAL_CODEC, stack);
                }
            },
            buf -> {
                ItemStack stack = buf instanceof net.minecraft.network.RegistryFriendlyByteBuf registry
                        ? ItemStack.OPTIONAL_STREAM_CODEC.decode(registry)
                        : buf.readJsonWithCodec(ItemStack.OPTIONAL_CODEC);
                return of(stack);
            });

    public net.minecraft.nbt.CompoundTag toNbt() {
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.putString("id", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString());
        return tag;
    }

    public static ItemVariant fromNbt(net.minecraft.nbt.CompoundTag tag) {
        if (tag == null) {
            return BLANK;
        }
        return of(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(tag.getString("id"))));
    }

    public net.minecraft.nbt.CompoundTag getNbt() {
        return null;
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public ItemStack toStack(int count) {
        if (isBlank()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item, count);
        stack.applyComponents(components);
        return stack;
    }
}
