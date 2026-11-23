/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * One FilterList entry. Item filters match the item. Tag filters match a tag.
 * Inverted entries match when the inner test fails, as upstream Entry.matches does.
 */

package com.neep.neepmeat.machine.transport;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record FilterEntry(Kind kind, Item item, TagKey<Item> tag, boolean inverted) {
    public enum Kind { ITEM, TAG }

    public static FilterEntry item(Item item, boolean inverted) {
        return new FilterEntry(Kind.ITEM, item, null, inverted);
    }

    public static FilterEntry tag(TagKey<Item> tag, boolean inverted) {
        return new FilterEntry(Kind.TAG, null, tag, inverted);
    }

    public boolean matches(ItemStack stack) {
        boolean hit = kind == Kind.ITEM ? stack.is(item) : tag != null && stack.is(tag);
        return hit != inverted;
    }
}
