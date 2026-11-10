/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Three filter slots. Each slot is an item entry. The screen toggles inverted
 * and switches an entry to the item's first tag.
 */

package com.neep.neepmeat.machine.transport;

import com.neep.neepmeat.machine.NeepMachines;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FilterPipeMenu extends AbstractContainerMenu {
    private final ItemPipeBlockEntity pipe;
    private final SimpleContainer slots = new SimpleContainer(3);

    public FilterPipeMenu(int id, Inventory inventory, ItemPipeBlockEntity pipe) {
        super(NeepMachines.FILTER_MENU.get(), id);
        this.pipe = pipe;
        for (int i = 0; i < pipe.entries().size() && i < 3; i++) {
            FilterEntry entry = pipe.entries().get(i);
            if (entry.kind() == FilterEntry.Kind.ITEM && entry.item() != null) {
                slots.setItem(i, new ItemStack(entry.item()));
            }
        }
        for (int i = 0; i < 3; i++) {
            int index = i;
            addSlot(new Slot(slots, i, 44 + i * 18, 20) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    ItemStack stack = getItem();
                    if (stack.isEmpty()) {
                        return;
                    }
                    boolean inverted = index < pipe.entries().size() && pipe.entries().get(index).inverted();
                    pipe.setEntry(index, FilterEntry.item(stack.getItem(), inverted));
                }
            });
        }
    }

    public ItemPipeBlockEntity pipe() {
        return pipe;
    }

    public void toggleInverted(int index) {
        if (index < 0 || index >= pipe.entries().size()) {
            return;
        }
        FilterEntry entry = pipe.entries().get(index);
        pipe.setEntry(index, new FilterEntry(entry.kind(), entry.item(), entry.tag(), !entry.inverted()));
    }

    public void useFirstTag(int index) {
        ItemStack stack = slots.getItem(index);
        if (stack.isEmpty()) {
            return;
        }
        stack.getItem().builtInRegistryHolder().tags().findFirst().ifPresent(tag -> {
            boolean inverted = index < pipe.entries().size() && pipe.entries().get(index).inverted();
            pipe.setEntry(index, FilterEntry.tag(tag, inverted));
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(pipe.getBlockPos().getCenter()) < 64;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < 3) {
            toggleInverted(id);
            return true;
        }
        if (id >= 3 && id < 6) {
            useFirstTag(id - 3);
            return true;
        }
        return false;
    }
}
