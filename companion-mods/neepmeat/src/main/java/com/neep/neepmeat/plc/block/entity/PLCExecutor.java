/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.block.entity;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.plc.instruction.Instruction;

public interface PLCExecutor {
    void receiveInstruction(Instruction instruction) throws NeepASM.RuntimeException;
}
