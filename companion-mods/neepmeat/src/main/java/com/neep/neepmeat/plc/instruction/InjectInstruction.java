/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.instruction;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.plc.robot.AtomicAction;
import com.neep.neepmeat.api.plc.robot.DelayAction;
import com.neep.neepmeat.api.plc.robot.GroupedRobotAction;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.entity.CombineActuator;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.neepmeat.plc.robot.RobotMoveToAction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class InjectInstruction implements PlcInstruction {
    public static final int BUCKET_MB = 1000;

    private final Supplier<Level> world;
    private final Argument from;
    private final Argument to;
    private @Nullable FluidStack stored;
    private final GroupedRobotAction group;

    public InjectInstruction(Supplier<Level> world, List<Argument> arguments) {
        this.world = world;
        this.from = arguments.get(0);
        this.to = arguments.get(1);
        group = GroupedRobotAction.of(
                new RobotMoveToAction(from.pos()),
                AtomicAction.of(this::playTakeSound),
                AtomicAction.of(this::takeFrom),
                new Glue(to.pos()),
                AtomicAction.of(this::playSound),
                new DelayAction(40),
                AtomicAction.of(this::insert)
        );
    }

    public InjectInstruction(Supplier<Level> world, CompoundTag nbt) {
        this(world, List.of(
                Argument.fromNbt(nbt.getCompound("from")),
                Argument.fromNbt(nbt.getCompound("to"))));
        FluidStack loaded = PlcInstruction.readFluid(nbt.getCompound("stored"));
        this.stored = loaded.isEmpty() ? null : loaded;
    }

    private void playTakeSound(PLC plc) {
        var robot = plc.getActuator();
        world.get().playSound(null, robot.getX(), robot.getY(), robot.getZ(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1, 1);
    }

    private void playSound(PLC plc) {
        var robot = plc.getActuator();
        world.get().playSound(null, robot.getX(), robot.getY(), robot.getZ(), SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1, 1);
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt) {
        nbt.put("from", from.toNbt());
        nbt.put("to", to.toNbt());
        nbt.put("stored", PlcInstruction.writeFluid(stored));
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
            throw NeepASM.RuntimeException.notSupported("INJECT");
        }
    }

    @Override
    public void cancel(PLC plc) {
        group.end(plc);
    }

    private void takeFrom(PLC plc) throws NeepASM.RuntimeException {
        FluidStack taken = Instructions.takeFluid(from, world, BUCKET_MB);
        if (!taken.isEmpty()) {
            this.stored = taken;
        } else {
            throw new NeepASM.RuntimeException("Insufficient or no fluid at " + from);
        }
    }

    private void returnFluid() {
        if (stored != null && !stored.isEmpty()) {
            Instructions.giveFluid(from, world, stored);
            stored = null;
        }
    }

    private void insert(PLC plc) {
        stored = null;
    }

    private void finish(PLC plc) {
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode() {
        return Instructions.INJECT;
    }

    private class Glue extends RobotMoveToAction {
        public Glue(BlockPos target) {
            super(target);
        }

        @Override
        public void start(PLC plc) throws NeepASM.RuntimeException {
            super.start(plc);
            if (MutateInPlace.ITEM.find(world.get(), to.pos(), null) != null) {
                return;
            }
            var entity = MutateInPlace.ENTITY.find(world.get(), to.pos(), null);
            if (entity != null) {
                return;
            }
            returnFluid();
            throw new NeepASM.RuntimeException("INJECT: No item or entity");
        }
    }
}
