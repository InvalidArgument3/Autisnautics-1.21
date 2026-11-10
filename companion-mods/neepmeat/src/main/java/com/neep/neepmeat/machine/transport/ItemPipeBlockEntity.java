/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine.transport;

import com.neep.neepmeat.machine.NeepMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class ItemPipeBlockEntity extends BlockEntity {
    private ItemStack buffer = ItemStack.EMPTY;
    private int cooldown;

    public ItemPipeBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.ITEM_PIPE_BE.get(), pos, state);
    }

    public void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        if (cooldown-- > 0) {
            return;
        }
        cooldown = 5;
        Direction facing = getBlockState().getValue(ItemPipeBlock.FACING);
        if (!buffer.isEmpty()) {
            ItemStack leftover = insert(level, worldPosition.relative(facing), facing.getOpposite(), buffer);
            if (leftover.getCount() != buffer.getCount()) {
                buffer = leftover;
                setChanged();
            }
            return;
        }
        buffer = extract(level, worldPosition.relative(facing.getOpposite()), facing, 1);
        if (!buffer.isEmpty()) {
            setChanged();
        }
    }

    private static ItemStack insert(Level level, BlockPos pos, Direction from, ItemStack stack) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, from);
        if (handler == null) {
            return stack;
        }
        return ItemHandlerHelper.insertItem(handler, stack, false);
    }

    private static ItemStack extract(Level level, BlockPos pos, Direction from, int amount) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, from);
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack extracted = handler.extractItem(slot, amount, false);
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }
}
