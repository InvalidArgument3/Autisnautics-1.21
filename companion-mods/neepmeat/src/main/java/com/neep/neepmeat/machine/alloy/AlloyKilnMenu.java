/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine.alloy;

import com.neep.neepmeat.machine.NeepMachines;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AlloyKilnMenu extends AbstractContainerMenu {
    private final AlloyKilnBlockEntity kiln;

    public AlloyKilnMenu(int id, Inventory inventory, AlloyKilnBlockEntity kiln) {
        super(NeepMachines.ALLOY_MENU.get(), id);
        this.kiln = kiln;
        addSlot(new Slot(kiln, AlloyKilnBlockEntity.FUEL, 56, 53));
        addSlot(new Slot(kiln, AlloyKilnBlockEntity.INPUT_1, 47, 17));
        addSlot(new Slot(kiln, AlloyKilnBlockEntity.INPUT_2, 65, 17));
        addSlot(new Slot(kiln, AlloyKilnBlockEntity.OUTPUT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return kiln.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            original = stack.copy();
            if (index < 4) {
                if (!moveItemStackTo(stack, 4, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, 3, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return original;
    }
}
