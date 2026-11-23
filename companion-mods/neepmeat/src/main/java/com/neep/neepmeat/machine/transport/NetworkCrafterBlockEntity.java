/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Holds ingredients delivered by recursive requests. When every ingredient is
 * present it crafts one result into the output slot.
 */

package com.neep.neepmeat.machine.transport;

import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.neepmeat.machine.NeepMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class NetworkCrafterBlockEntity extends BlockEntity implements ImplementedInventory, IItemHandler {
    private final NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private final List<Item> ingredients = new ArrayList<>();
    private Item result = net.minecraft.world.item.Items.AIR;

    public NetworkCrafterBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.NETWORK_CRAFTER_BE.get(), pos, state);
    }

    public void setRecipe(Item result, List<Item> ingredients) {
        this.result = result;
        this.ingredients.clear();
        this.ingredients.addAll(ingredients);
        setChanged();
    }

    public Item result() {
        return result;
    }

    public List<Item> ingredients() {
        return List.copyOf(ingredients);
    }

    public boolean has(Item item) {
        for (int i = 0; i < 4; i++) {
            if (items.get(i).is(item)) {
                return true;
            }
        }
        return false;
    }

    public void serverTick() {
        if (result == net.minecraft.world.item.Items.AIR || !items.get(4).isEmpty()) {
            return;
        }
        for (Item ingredient : ingredients) {
            if (!has(ingredient)) {
                return;
            }
        }
        for (Item ingredient : ingredients) {
            for (int i = 0; i < 4; i++) {
                if (items.get(i).is(ingredient)) {
                    items.get(i).shrink(1);
                    break;
                }
            }
        }
        items.set(4, new ItemStack(result));
        setChanged();
    }

    @Override
    public NonNullList<ItemStack> items() {
        return items;
    }

    @Override
    public int getSlots() {
        return 5;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot >= 4 || stack.isEmpty() || !items.get(slot).isEmpty()) {
            return stack;
        }
        if (!simulate) {
            items.set(slot, stack.copyWithCount(1));
            setChanged();
        }
        return stack.getCount() == 1 ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - 1);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 4 || items.get(4).isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = items.get(4).copy();
        if (!simulate) {
            items.set(4, ItemStack.EMPTY);
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

    public ItemStack crafted() {
        return items.get(4);
    }
}
