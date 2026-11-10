/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.plc.instruction;

import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.plc.robot.AtomicAction;
import com.neep.neepmeat.api.plc.robot.GroupedRobotAction;
import com.neep.neepmeat.init.NMSounds;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.entity.CombineActuator;
import com.neep.neepmeat.plc.robot.RobotMoveToAction;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class MoveInstruction implements PlcInstruction
{
    private final Supplier<Level> world;
    private final GroupedRobotAction group;
    private final Argument from;
    private final Argument to;
    private final int amount;
    private final Pattern pattern;

    public MoveInstruction(Supplier<Level> world, List<Argument> arguments)
    {
        this(world, arguments.get(0), arguments.get(1), 64, ".*");
    }

    public MoveInstruction(Supplier<Level> world, Argument from, Argument to, int count, String regex)
    {
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

    public MoveInstruction(Supplier<Level> world, CompoundTag nbt)
    {
        this(world,
                Argument.fromNbt(nbt.getCompound("from")),
                Argument.fromNbt(nbt.getCompound("to")),
                nbt.getInt("amount"),
                nbt.contains("regex") ? nbt.getString("regex") : ".*"
            );
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.put("from", from.toNbt());
        nbt.put("to", to.toNbt());
        nbt.putInt("amount", amount);
        nbt.putString("regex", pattern.pattern());
        return nbt;
    }

    @Override
    public void writeVolatileState(CompoundTag nbt)
    {
        group.writeNbt(nbt);
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        group.readNbt(plc.volatileStorage());

        if (plc.getActuator() instanceof CombineActuator)
            plc.addRobotAction(group, this::finish);
        else
            throw NeepASM.RuntimeException.notSupported("MOVE");
    }

    @Override
    public void cancel(PLC plc)
    {
        plc.getActuator().dumpStored(plc);

        group.end(plc);
    }

    private void takeFirst(PLC plc) throws NeepASM.RuntimeException
    {
        var stored = Instructions.takeItem(from, world, amount, pattern);
        if (stored == null)
        {
            group.setFinished(plc);
            plc.dataStack().push(0); // false
            plc.advanceCounter();
        }
        else
        {
            var robot = plc.getActuator();
            robot.setStored(plc, stored);
            world.get().playSeededSound(null, robot.getX(), robot.getY(), robot.getZ(), NMSounds.EXTRACT_ITEM, SoundSource.BLOCKS, 1, 1, 1);
        }
    }

    private void complete(PLC plc) throws NeepASM.RuntimeException
    {
        var robot = plc.getActuator();
        var stored = robot.getStored(plc);
        var storage = ItemStorage.SIDED.find(world.get(), to.pos(), to.face());

        if (stored != null && storage != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                long inserted = storage.insert(stored.resource(), stored.amount(), transaction);
                if (inserted == stored.amount())
                {
                    transaction.commit();
                    world.get().playSeededSound(null, robot.getX(), robot.getY(), robot.getZ(), NMSounds.INSERT_ITEM, SoundSource.BLOCKS, 1, 1, 1);
                    robot.setStored(plc, null);
                    plc.dataStack().push(-1);
                    return;
                }
                else
                {
                    transaction.abort();
                }
            }
        }

        robot.dumpStored(plc);
    }

    private void finish(PLC plc) throws NeepASM.RuntimeException
    {
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.MOVE;
    }

    public static PlcParsedInstruction parser(NeepAsmTokenView view, NeepAsmParser parser) throws NeepASM.ParseException
    {
        view.fastForward();
        Argument from = parser.parseWorldTarget(view);
        if (from == null)
            throw new NeepASM.ParseException("expected source world target");

        view.fastForward();
        Argument to = parser.parseWorldTarget(view);
        if (to == null)
            throw new NeepASM.ParseException("expected destination world target");

        view.fastForward();

        int count;
        if (NeepAsmTokenView.isDigit(view.peek()))
            count = view.nextInteger();
        else
            count = 64;

        view.fastForward();

        String regex;
        if (view.peek() == '"')
            regex = Instructions.parseRegistryGlob(view, BuiltInRegistries.ITEM, "item", false);
        else
            regex = ".*";

        parser.assureLineEnd(view);

        return (world, parsedSource1, program) ->
        {
            program.addBack(new MoveInstruction(() -> world, from, to, count, regex));
        };
    }
}
