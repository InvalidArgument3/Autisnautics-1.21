/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.block.entity;

import com.neep.neepmeat.machine.NeepMachines;
import com.neep.neepmeat.plc.block.RedstoneInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PLCRedstoneInterfaceBlockEntity extends BlockEntity implements RedstoneInterface {
    private int emitted;
    private int received;

    public PLCRedstoneInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.REDSTONE_INTERFACE_BE.get(), pos, state);
    }

    @Override
    public int getReceivedStrength() {
        return received;
    }

    public int getEmittedStrength() {
        return emitted;
    }

    @Override
    public void setEmittedStrength(int strength) {
        int clamped = Math.max(0, Math.min(15, strength));
        if (emitted != clamped) {
            emitted = clamped;
            setChanged();
            if (level != null) {
                level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
            }
        }
    }

    public void updateReceived(int received) {
        if (this.received != received) {
            this.received = received;
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("emitted", emitted);
        tag.putInt("received", received);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        emitted = tag.getInt("emitted");
        received = tag.getInt("received");
    }
}
