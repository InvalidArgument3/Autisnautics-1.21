/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package dev.onyxstudios.cca.api.v3.item;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Cardinal Components' item component, stored in the stack's {@link DataComponents#CUSTOM_DATA}.
 * When a {@link ComponentKey} is supplied, the component's tag is the compound under that key's id,
 * which is how upstream kept components from sharing one tag.
 */
public abstract class ItemComponent implements Component {
    protected final ItemStack stack;
    @Nullable
    protected final ComponentKey<?> key;

    public ItemComponent(ItemStack stack) {
        this(stack, null);
    }

    public ItemComponent(ItemStack stack, @Nullable ComponentKey<?> key) {
        this.stack = stack;
        this.key = key;
    }

    public ItemStack getStack() {
        return stack;
    }

    /** A copy of this component's compound. Mutations persist only through the put helpers. */
    public CompoundTag getOrCreateRootTag() {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (key == null || key.getId() == null) {
            return root;
        }
        String id = key.getId().toString();
        return root.contains(id, Tag.TAG_COMPOUND) ? root.getCompound(id) : new CompoundTag();
    }

    public CompoundTag getRootTag() {
        return getOrCreateRootTag();
    }

    public boolean contains(String name) {
        return getOrCreateRootTag().contains(name);
    }

    public void remove(String name) {
        edit(tag -> tag.remove(name));
    }

    public void putString(String name, String value) {
        edit(tag -> tag.putString(name, value));
    }

    public String getString(String name) {
        return getOrCreateRootTag().getString(name);
    }

    public void putBoolean(String name, boolean value) {
        edit(tag -> tag.putBoolean(name, value));
    }

    public boolean getBoolean(String name) {
        return getOrCreateRootTag().getBoolean(name);
    }

    public void putInt(String name, int value) {
        edit(tag -> tag.putInt(name, value));
    }

    public int getInt(String name) {
        return getOrCreateRootTag().getInt(name);
    }

    public void putLong(String name, long value) {
        edit(tag -> tag.putLong(name, value));
    }

    public long getLong(String name) {
        return getOrCreateRootTag().getLong(name);
    }

    public void putFloat(String name, float value) {
        edit(tag -> tag.putFloat(name, value));
    }

    public float getFloat(String name) {
        return getOrCreateRootTag().getFloat(name);
    }

    public void putCompound(String name, CompoundTag value) {
        edit(tag -> tag.put(name, value));
    }

    public CompoundTag getCompound(String name) {
        CompoundTag root = getOrCreateRootTag();
        return root.contains(name, Tag.TAG_COMPOUND) ? root.getCompound(name) : new CompoundTag();
    }

    public ListTag getList(String name, int type) {
        return getOrCreateRootTag().getList(name, type);
    }

    private void edit(java.util.function.Consumer<CompoundTag> action) {
        if (key == null || key.getId() == null) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, action);
            return;
        }
        String id = key.getId().toString();
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root -> {
            CompoundTag child = root.contains(id, Tag.TAG_COMPOUND) ? root.getCompound(id) : new CompoundTag();
            action.accept(child);
            root.put(id, child);
        });
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag) {
        edit(root -> {
            for (String name : tag.getAllKeys()) {
                root.put(name, tag.get(name));
            }
        });
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag) {
        CompoundTag root = getOrCreateRootTag();
        for (String name : root.getAllKeys()) {
            tag.put(name, root.get(name));
        }
    }
}
