/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.instruction;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.RedstoneInterface;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ReadRedstoneInstruction implements PlcInstruction {
    private final Supplier<Level> world;
    private final Argument target;

    public ReadRedstoneInstruction(Supplier<Level> world, Argument argument) {
        this.world = world;
        this.target = argument;
    }

    public ReadRedstoneInstruction(Supplier<Level> world, CompoundTag nbt) {
        this(world, Argument.fromNbt(nbt.getCompound("target")));
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt) {
        nbt.put("target", target.toNbt());
        return nbt;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException {
        RedstoneInterface redstone = RedstoneInterface.find(world.get(), target.pos());
        if (redstone != null) {
            plc.dataStack().push(redstone.getReceivedStrength());
        }
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode() {
        return Instructions.READ_REDSTONE;
    }

    public static PlcParsedInstruction parser(NeepAsmTokenView view, NeepAsmParser parser, String scope) throws NeepASM.ParseException {
        view.fastForward();
        Argument target = parser.parseWorldTarget(view);
        if (target == null) {
            throw new NeepASM.ParseException("expected redstone target");
        }
        return (world, source, program) -> program.addBack(new ReadRedstoneInstruction(() -> world, target));
    }
}
