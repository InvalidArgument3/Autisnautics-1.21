/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Every ten ticks, take one item from an adjacent inventory and insert it into
 * an adjacent pipe using the route PipeRouteFinder found. Insertion is refused
 * when simulate says the destination cannot hold the stack.
 */

package com.neep.neepmeat.machine.transport;

import com.neep.neepmeat.machine.NeepMachines;
import com.neep.neepmeat.transport.ItemRoute;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public class PipeDriverBlockEntity extends BlockEntity {
    private int cooldown;

    public PipeDriverBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.PIPE_DRIVER_BE.get(), pos, state);
    }

    public void serverTick() {
        if (level == null || cooldown-- > 0) {
            return;
        }
        cooldown = 10;
        boolean valid = false;
        for (Direction towardPipe : Direction.values()) {
            BlockPos pipePos = worldPosition.relative(towardPipe);
            if (!(level.getBlockEntity(pipePos) instanceof ItemPipeBlockEntity pipe)) {
                continue;
            }
            for (Direction towardInventory : Direction.values()) {
                if (towardInventory == towardPipe) {
                    continue;
                }
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, worldPosition.relative(towardInventory), towardInventory.getOpposite());
                if (handler == null) {
                    continue;
                }
                valid = true;
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack extracted = handler.extractItem(slot, 1, true);
                    if (extracted.isEmpty()) {
                        continue;
                    }
                    ItemRoute route = PipeRouteFinder.find(level, pipePos, towardPipe.getOpposite(), extracted);
                    if (route.isEmpty()) {
                        continue;
                    }
                    if (pipe.insert(extracted, towardPipe.getOpposite(), route) == extracted.getCount()) {
                        handler.extractItem(slot, 1, false);
                        setValid(true);
                        return;
                    }
                }
            }
        }
        setValid(valid);
    }

    private void setValid(boolean valid) {
        if (getBlockState().getValue(PipeDriverBlock.VALID) != valid) {
            level.setBlock(worldPosition, getBlockState().setValue(PipeDriverBlock.VALID, valid), Block.UPDATE_CLIENTS);
        }
    }
}
