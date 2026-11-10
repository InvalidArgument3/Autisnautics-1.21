/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Holds the requested item and one output stack. Every ten ticks it asks the
 * network, matching the driver's request period.
 */

package com.neep.neepmeat.machine.transport;

import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.neepmeat.machine.NeepMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public class ItemRequesterBlockEntity extends BlockEntity implements ImplementedInventory, IItemHandler {
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private ItemStack requested = ItemStack.EMPTY;
    private int cooldown;

    public ItemRequesterBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.ITEM_REQUESTER_BE.get(), pos, state);
    }

    public void request(ItemStack stack) {
        this.requested = stack.copyWithCount(1);
        setChanged();
    }

    public void serverTick() {
        if (level == null || requested.isEmpty() || cooldown-- > 0 || !getItem(0).isEmpty()) {
            return;
        }
        cooldown = 10;
        RoutingNetwork.request(level, worldPosition, requested);
    }

    @Override
    public NonNullList<ItemStack> items() {
        return items;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !requested.isEmpty() && !ItemStack.isSameItem(stack, requested)) {
            return stack;
        }
        if (!items.get(0).isEmpty()) {
            return stack;
        }
        if (!simulate) {
            items.set(0, stack.copy());
            setChanged();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = items.get(0);
        if (stack.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = stack.copy();
        if (!simulate) {
            items.set(0, ItemStack.EMPTY);
            setChanged();
        }
        return taken;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    public ItemStack output() {
        return getItem(0);
    }
}
