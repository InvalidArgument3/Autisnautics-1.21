/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.item.util;

import com.neep.meatlib.transfer.ItemVariant;
import net.minecraft.nbt.Tag;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class TransformingTools
{
    public static final String ROOT_ID = "neepmeat:transformingTool";

    public static boolean isTransformingTool(ItemStack stack)
    {
        CompoundTag nbt = stack.getTag();
        return nbt != null && nbt.contains(ROOT_ID);
    }

    public static boolean isTransformingTool(ItemVariant variant)
    {
        CompoundTag nbt = variant.getNbt();
        return nbt != null && nbt.contains(ROOT_ID);
    }

    public static void swap(ItemStack oldStack, Player player)
    {
        if (isTransformingTool(oldStack))
        {
            CompoundTag nbt = oldStack.getOrCreateTag().getCompound(ROOT_ID);

            // Retrieve alternate stack from NBT
            ItemStack newStack = ItemStack.of(nbt);
            
            // Clear oldStack's transforming tool NBT to avoid infinitely recursive NBT
            oldStack.removeTagKey(ROOT_ID);
            CompoundTag oldStackNbt = oldStack.save(new CompoundTag());

            // Convert the old stack to NBT and store it in the new stack
            CompoundTag newStackNbt = newStack.getOrCreateTag();
            newStackNbt.put(ROOT_ID, oldStackNbt);

            player.setItemInHand(InteractionHand.MAIN_HAND, newStack);
        }
    }

    public static ItemVariant combine(ItemVariant variant1, ItemVariant variant2)
    {
        if (isTransformingTool(variant1) || isTransformingTool(variant2))
            return null;

        CompoundTag nbt1 = variant1.getNbt() != null ? variant1.copyNbt() : new CompoundTag();

        ItemStack itemStack2 = variant2.toStack();
        setLore(itemStack2.getOrCreateTag(), Component.literal("[Transforming Item]").withStyle(ChatFormatting.DARK_RED));
        setLore(nbt1, Component.literal("[Transforming Item]").withStyle(ChatFormatting.DARK_RED));

        nbt1.put(ROOT_ID, itemStack2.save(new CompoundTag()));

        return ItemVariant.of(variant1.getItem(), nbt1);
    }

    protected static <T extends Tag> T getOrCreate(CompoundTag nbt, String key, int type, Supplier<T> supplier)
    {
        Tag element = nbt.get(key);
        if (element == null || element.getId() != type)
        {
            element = supplier.get();
            nbt.put(key, element);
        }
        return (T) element;
    }

    public static CompoundTag getOrCreateSubNbt(CompoundTag root, String key)
    {
        if (root == null || !root.contains(key, 10))
        {
            CompoundTag nbtCompound = new CompoundTag();
            root.put(key, nbtCompound);
            return nbtCompound;
        }
        return root.getCompound(key);
    }

    public static CompoundTag setLore(CompoundTag nbt, Component lore)
    {
        CompoundTag displayCompound = getOrCreateSubNbt(nbt, ItemStack.TAG_DISPLAY);
        ListTag loreList = getOrCreate(displayCompound, "Lore", Tag.TAG_LIST, ListTag::new);
        if (lore != null)
        {
            loreList.add(StringTag.valueOf(Component.Serializer.toJson(lore)));
        }
        else
        {
            displayCompound.remove(ItemStack.TAG_LORE);
        }
        nbt.put(ItemStack.TAG_DISPLAY, displayCompound);
        return nbt;
    }
}