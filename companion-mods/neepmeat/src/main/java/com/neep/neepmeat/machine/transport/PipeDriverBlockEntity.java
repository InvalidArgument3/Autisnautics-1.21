/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * The driver is valid while a pipe touches it. Requests are served by
 * RoutingNetwork, not by pulling every inventory the driver itself touches.
 */

package com.neep.neepmeat.machine.transport;

import com.neep.neepmeat.machine.NeepMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PipeDriverBlockEntity extends BlockEntity {
    public PipeDriverBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.PIPE_DRIVER_BE.get(), pos, state);
    }

    public void serverTick() {
        if (level == null) {
            return;
        }
        boolean valid = false;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(worldPosition.relative(direction)).getBlock() instanceof ItemPipeBlock) {
                valid = true;
                break;
            }
        }
        if (getBlockState().getValue(PipeDriverBlock.VALID) != valid) {
            level.setBlock(worldPosition, getBlockState().setValue(PipeDriverBlock.VALID, valid), Block.UPDATE_CLIENTS);
        }
    }
}
