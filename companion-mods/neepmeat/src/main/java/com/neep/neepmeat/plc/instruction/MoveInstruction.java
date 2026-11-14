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
import com.neep.neepmeat.api.plc.robot.AtomicAction;
import com.neep.neepmeat.api.plc.robot.GroupedRobotAction;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.entity.CombineActuator;
import com.neep.neepmeat.plc.robot.RobotMoveToAction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class MoveInstruction implements PlcInstruction {
    private final Supplier<Level> world;
    private final GroupedRobotAction group;
    private final Argument from;
    private final Argument to;
    private final int amount;
    private final Pattern pattern;

    public MoveInstruction(Supplier<Level> world, List<Argument> arguments) {
        this(world, arguments.get(0), arguments.get(1), 64, ".*");
    }

    public MoveInstruction(Supplier<Level> world, Argument from, Argument to, int count, String regex) {
        this.world = world;
        this.from = from;
        this.to = to;
        this.amount = count;
        this.pattern = Pattern.compile(regex);
        group = GroupedRobotAction.of(
                new RobotMoveToAction(from.pos()),
                AtomicAction.of(this::takeFirst),
                new RobotMoveToAction(to.pos()),
                AtomicAction.of(this::complete)
        );
    }

    public MoveInstruction(Supplier<Level> world, CompoundTag nbt) {
        this(world,
                Argument.fromNbt(nbt.getCompound("from")),
                Argument.fromNbt(nbt.getCompound("to")),
                nbt.getInt("amount"),
                nbt.contains("regex") ? nbt.getString("regex") : ".*");
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt) {
        nbt.put("from", from.toNbt());
        nbt.put("to", to.toNbt());
        nbt.putInt("amount", amount);
        nbt.putString("regex", pattern.pattern());
        return nbt;
    }

    @Override
    public void writeVolatileState(CompoundTag nbt) {
        group.writeNbt(nbt);
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException {
        group.readNbt(plc.volatileStorage());
        if (plc.getActuator() instanceof CombineActuator) {
            plc.addRobotAction(group, this::finish);
        } else {
            throw NeepASM.RuntimeException.notSupported("MOVE");
        }
    }

    @Override
    public void cancel(PLC plc) {
        plc.getActuator().dumpStored(plc);
        group.end(plc);
    }

    private void takeFirst(PLC plc) throws NeepASM.RuntimeException {
        ItemStack stored = Instructions.takeItem(from, world, amount, pattern);
        if (stored.isEmpty()) {
            group.setFinished(plc);
            plc.dataStack().push(0);
            plc.advanceCounter();
        } else {
            var robot = plc.getActuator();
            robot.setStored(plc, stored);
            world.get().playSound(null, robot.getX(), robot.getY(), robot.getZ(), SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.BLOCKS, 1, 1);
        }
    }

    private void complete(PLC plc) throws NeepASM.RuntimeException {
        var robot = plc.getActuator();
        ItemStack stored = robot.getStored(plc);
        if (!stored.isEmpty()) {
            int inserted = Instructions.giveItem(to, world, stored);
            if (inserted == stored.getCount()) {
                world.get().playSound(null, robot.getX(), robot.getY(), robot.getZ(), SoundEvents.BUNDLE_INSERT, SoundSource.BLOCKS, 1, 1);
                robot.setStored(plc, ItemStack.EMPTY);
                plc.dataStack().push(-1);
                return;
            }
        }
        robot.dumpStored(plc);
        plc.dataStack().push(0);
    }

    private void finish(PLC plc) throws NeepASM.RuntimeException {
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode() {
        return Instructions.MOVE;
    }

    public static PlcParsedInstruction parser(NeepAsmTokenView view, NeepAsmParser parser) throws NeepASM.ParseException {
        view.fastForward();
        Argument from = parser.parseWorldTarget(view);
        if (from == null) {
            throw new NeepASM.ParseException("expected source world target");
        }
        view.fastForward();
        Argument to = parser.parseWorldTarget(view);
        if (to == null) {
            throw new NeepASM.ParseException("expected destination world target");
        }
        view.fastForward();
        int count = NeepAsmTokenView.isDigit(view.last()) ? view.nextInteger() : 64;
        view.fastForward();
        String regex = view.last() == '"'
                ? Instructions.parseRegistryGlob(view, BuiltInRegistries.ITEM, "item", false)
                : ".*";
        parser.assureLineEnd(view);
        return (world, parsedSource, program) -> program.addBack(new MoveInstruction(() -> world, from, to, count, regex));
    }
}
