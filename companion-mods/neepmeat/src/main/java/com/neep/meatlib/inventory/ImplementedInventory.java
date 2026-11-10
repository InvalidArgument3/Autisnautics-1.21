/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.inventory;

import com.neep.meatlib.util.NbtSerialisable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The upstream Fabric inventory helper, on 1.21 NBT. Item stacks inside a block entity still
 * save as NBT. Loose item data that used to be NBT on the stack belongs in data components.
 */
public interface ImplementedInventory extends Container, NbtSerialisable {
    static ImplementedInventory of(NonNullList<ItemStack> items) {
        return new ListInventory(items, () -> {});
    }

    static ImplementedInventory of(NonNullList<ItemStack> items, Runnable onChange) {
        return new ListInventory(items, onChange);
    }

    static ImplementedInventory ofSize(int size) {
        return of(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    static ImplementedInventory ofSize(int size, Runnable onChange) {
        return of(NonNullList.withSize(size, ItemStack.EMPTY), onChange);
    }

    NonNullList<ItemStack> items();

    @Override
    default int getContainerSize() {
        return items().size();
    }

    @Override
    default boolean isEmpty() {
        for (ItemStack stack : items()) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    default ItemStack getItem(int slot) {
        return items().get(slot);
    }

    @Override
    default ItemStack removeItem(int slot, int amount) {
        ItemStack split = items().get(slot).split(amount);
        if (!split.isEmpty()) {
            setChanged();
        }
        return split;
    }

    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = items().get(slot);
        items().set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    default void setItem(int slot, ItemStack stack) {
        items().set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    default boolean stillValid(Player player) {
        return true;
    }

    @Override
    default void clearContent() {
        items().clear();
        setChanged();
    }

    @Override
    default CompoundTag writeNbt(CompoundTag nbt) {
        return writeNbt(nbt, null);
    }

    default CompoundTag writeNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (int slot = 0; slot < items().size(); slot++) {
            ItemStack stack = items().get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putByte("Slot", (byte) slot);
            if (registries != null) {
                entry.put("Stack", stack.save(registries));
            } else {
                entry.putString("id", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                entry.putInt("count", stack.getCount());
            }
            list.add(entry);
        }
        nbt.put("Items", list);
        return nbt;
    }

    @Override
    default void readNbt(CompoundTag nbt) {
        readNbt(nbt, null);
    }

    default void readNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        items().clear();
        ListTag list = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot < 0 || slot >= items().size()) {
                continue;
            }
            ItemStack stack;
            if (registries != null && entry.contains("Stack")) {
                stack = ItemStack.parseOptional(registries, entry.getCompound("Stack"));
            } else if (entry.contains("id")) {
                var id = net.minecraft.resources.ResourceLocation.parse(entry.getString("id"));
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id);
                stack = item == null ? ItemStack.EMPTY : new ItemStack(item, entry.getInt("count"));
            } else {
                stack = ItemStack.EMPTY;
            }
            items().set(slot, stack);
        }
    }
}

final class ListInventory implements ImplementedInventory {
    private final NonNullList<ItemStack> items;
    private final Runnable onChange;

    ListInventory(NonNullList<ItemStack> items, Runnable onChange) {
        this.items = items;
        this.onChange = onChange;
    }

    @Override
    public NonNullList<ItemStack> items() {
        return items;
    }

    @Override
    public void setChanged() {
        onChange.run();
    }

}
