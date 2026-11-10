/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * One stack. Upstream item ports wrap a Fabric item storage.
 */

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ItemPortBlockEntity extends BlockEntity implements LivingMachineComponent {
    private ItemStack stack = ItemStack.EMPTY;

    public ItemPortBlockEntity(BlockPos pos, BlockState state) {
        super(LivingMachines.ITEM_PORT_BE.get(), pos, state);
    }

    public ItemStack stack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack removeStack() {
        ItemStack taken = stack;
        stack = ItemStack.EMPTY;
        return taken;
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType() {
        return getBlockState().is(LivingMachines.ITEM_OUTPUT.get())
                ? LivingMachineComponents.ITEM_OUTPUT
                : LivingMachineComponents.ITEM_INPUT;
    }
}
