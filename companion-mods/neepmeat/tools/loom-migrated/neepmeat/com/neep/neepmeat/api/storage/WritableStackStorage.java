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

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class WritableStackStorage extends SingleVariantStorage<ItemVariant> implements StorageView<ItemVariant>
{
    protected final int capacity;
    protected Runnable callback;

    public WritableStackStorage(@Nullable Runnable parent)
    {
        this(parent, 64);
    }

    public WritableStackStorage(@Nullable Runnable parent, int capacity)
    {
        this.callback = parent;
        this.capacity = capacity;
    }

    @Override
    protected void onFinalCommit()
    {
        syncIfPossible();
    }

    public CompoundTag toNbt(CompoundTag nbt)
    {
        writeNbt(nbt);
        return nbt;
    }

    @Override
    public void writeNbt(CompoundTag nbt)
    {
        nbt.putLong("amount", getAmount());
        nbt.put("resource", getResource().toNbt());
    }

    public void readNbt(CompoundTag nbt)
    {
        this.amount = nbt.getLong("amount");
        this.variant = ItemVariant.fromNbt((CompoundTag) nbt.get("resource"));
    }

    public void syncIfPossible()
    {
        if (callback != null)
            callback.run();
    }

    public void setStack(ItemStack stack)
    {
        this.variant = ItemVariant.of(stack);
        this.amount = stack.getCount();
        syncIfPossible();
    }

    public ItemStack getAsStack()
    {
        return variant.toStack((int) getAmount());
    }

    public boolean isEmpty()
    {
        return this.amount == 0 || this.variant.isBlank();
    }

    @Override
    protected ItemVariant getBlankVariant()
    {
        return ItemVariant.blank();
    }

    @Override
    protected long getCapacity(ItemVariant variant)
    {
        if (!variant.isBlank())
        {
            return Math.min(variant.getItem().getMaxStackSize(), capacity);
        }
        return capacity;
    }

    public static boolean handleInteract(Player player, InteractionHand hand, SingleVariantStorage<ItemVariant> storage)
    {
        ItemStack stack = player.getItemInHand(hand);
        if ((stack.isEmpty() || !storage.getResource().matches(stack)) && !storage.isResourceBlank())
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                ItemVariant resource = storage.getResource();
                long extracted = storage.extract(resource, Long.MAX_VALUE, transaction);

                ItemStack giveStack = resource.toStack((int) extracted);
                boolean give = player.getInventory().add(giveStack);

                if (!giveStack.isEmpty())
                {
                    player.drop(giveStack, false);
                }

                transaction.commit();

                return true;
            }
        }
        else if (storage.isResourceBlank() && !stack.isEmpty())
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
                stack.shrink((int) inserted);
                transaction.commit();
                return true;
            }
        }
        return false;
    }

    public void extractDirect(int amount)
    {
        this.amount = Math.max(0, this.amount - amount);
        if (this.amount == 0)
        {
            this.variant = ItemVariant.blank();
        }
    }
}
