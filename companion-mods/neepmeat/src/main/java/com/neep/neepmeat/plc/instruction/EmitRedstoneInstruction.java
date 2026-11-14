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
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class EmitRedstoneInstruction implements PlcInstruction {
    private final Supplier<Level> world;
    private final Argument target;
    private final int strength;
    private final boolean useStack;

    public EmitRedstoneInstruction(Supplier<Level> world, Argument argument, int strength, boolean useStack) {
        this.world = world;
        this.target = argument;
        this.strength = strength;
        this.useStack = useStack;
    }

    public EmitRedstoneInstruction(Supplier<Level> world, CompoundTag nbt) {
        this.target = Argument.fromNbt(nbt.getCompound("target"));
        this.world = world;
        this.strength = nbt.getInt("strength");
        this.useStack = nbt.getBoolean("use_stack");
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt) {
        nbt.put("target", target.toNbt());
        nbt.putInt("strength", strength);
        nbt.putBoolean("use_stack", useStack);
        return nbt;
    }

    @Override
    public boolean canUse(PLC plc) {
        if (plc instanceof BlockEntity entity && entity.getLevel() != null) {
            return entity.getLevel().getGameTime() % 2 == 0;
        }
        return true;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException {
        RedstoneInterface redstone = RedstoneInterface.find(world.get(), target.pos());
        if (redstone != null) {
            redstone.setEmittedStrength(useStack ? plc.dataStack().pop().getAsInt() : strength);
        }
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode() {
        return Instructions.EMIT_REDSTONE;
    }

    public static PlcParsedInstruction parser(NeepAsmTokenView view, NeepAsmParser parser, String scope) throws NeepASM.ParseException {
        view.fastForward();
        Argument target = parser.parseWorldTarget(view);
        if (target == null) {
            throw new NeepASM.ParseException("expected redstone target");
        }
        view.fastForward();
        if (!NeepAsmTokenView.isDigit(view.last())) {
            parser.assureLineEnd(view);
            return (world, source, program) -> program.addBack(new EmitRedstoneInstruction(() -> world, target, 0, true));
        }
        int strength = view.nextInteger();
        return (world, source, program) -> program.addBack(new EmitRedstoneInstruction(() -> world, target, strength, false));
    }
}
