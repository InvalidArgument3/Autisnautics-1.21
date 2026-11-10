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

package com.neep.neepmeat.api.storage;

import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.SingleVariantStorage;
import net.minecraft.world.item.ItemStack;

public class WritableStackStorage extends SingleVariantStorage<ItemVariant> {
    private final int capacity;
    private final Runnable callback;

    public WritableStackStorage(Runnable callback) {
        this(callback, 64);
    }

    public WritableStackStorage(Runnable callback, int capacity) {
        super(ItemVariant.BLANK);
        this.callback = callback;
        this.capacity = capacity;
    }

    @Override
    protected ItemVariant blank() {
        return ItemVariant.BLANK;
    }

    @Override
    protected long capacity(ItemVariant resource) {
        if (resource != null && !resource.isBlank()) {
            return Math.min(resource.getItem().getDefaultMaxStackSize(), capacity);
        }
        return capacity;
    }

    @Override
    protected boolean matches(ItemVariant stored, ItemVariant offered) {
        return stored != null && offered != null && stored.getItem() == offered.getItem();
    }

    public void setStack(ItemStack stack) {
        this.variant = ItemVariant.of(stack);
        this.amount = stack.getCount();
        if (callback != null) {
            callback.run();
        }
    }

    public ItemStack getAsStack() {
        return variant == null ? ItemStack.EMPTY : variant.toStack((int) amount);
    }

    public boolean isEmpty() {
        return amount <= 0 || variant == null || variant.isBlank();
    }
}
