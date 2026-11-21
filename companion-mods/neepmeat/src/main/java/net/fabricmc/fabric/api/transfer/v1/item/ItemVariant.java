/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */


package net.fabricmc.fabric.api.transfer.v1.item;

import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemVariant extends TransferVariant<Item> {
    public static final ItemVariant BLANK = new ItemVariant();
    public static ItemVariant of(Item item) { return BLANK; }
    public static ItemVariant of(ItemStack stack) { return stack == null || stack.isEmpty() ? BLANK : new ItemVariant(); }
    public static ItemVariant fromNbt(CompoundTag tag) { return BLANK; }
    public static ItemVariant fromPacket(FriendlyByteBuf buf) { return BLANK; }
    public Item getItem() { return Items.AIR; }
    public ItemStack toStack() { return ItemStack.EMPTY; }
    public ItemStack toStack(int count) { return ItemStack.EMPTY; }
    @Override public boolean isBlank() { return true; }
}
