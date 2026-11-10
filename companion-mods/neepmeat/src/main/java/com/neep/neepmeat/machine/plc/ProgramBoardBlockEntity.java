/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine.plc;

import com.neep.neepmeat.machine.NeepMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ProgramBoardBlockEntity extends BlockEntity {
    private String program = "";

    public ProgramBoardBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.PROGRAM_BOARD_BE.get(), pos, state);
    }

    public String program() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program == null ? "" : program;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Program", program);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        program = tag.getString("Program");
    }
}
