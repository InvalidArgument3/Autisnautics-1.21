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

import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.plc.recipe.Workpiece;
import com.neep.neepmeat.api.plc.robot.GroupedRobotAction;
import com.neep.neepmeat.init.NMComponents;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.neepmeat.plc.recipe.ItemWorkpiece;
import com.neep.neepmeat.plc.robot.RobotMoveToAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RemoveInstruction implements PlcInstruction
{
    private final Supplier<Level> world;
    private final Argument from;

    public RemoveInstruction(Supplier<Level> world, List<Argument> arguments)
    {
        this.world = world;
        this.from = arguments.get(0);
    }

    public RemoveInstruction(Supplier<Level> world, CompoundTag compound)
    {
        this.world = world;
        this.from = Argument.fromNbt(compound.getCompound("target"));
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.put("target", from.toNbt());
        return nbt;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        plc.addRobotAction(GroupedRobotAction.of(
                new RobotMoveToAction(from.pos())
        ), this::finish);
    }

    private void finish(PLC plc)
    {
        MutateInPlace<?> mip = MutateInPlace.ITEM.find(world.get(), from.pos(), null);
        if (mip == null)
        {
            mip = MutateInPlace.ENTITY.find(world.get(), from.pos(), null);
        }

        if (mip != null)
        {
            var object = mip.get();
            if (object instanceof ItemStack stack && !ItemWorkpiece.has(stack))
            {
                return;
            }

            Workpiece workpiece;
            if (object != null && (workpiece = NMComponents.WORKPIECE.getNullable(object)) != null)
            {
                if (workpiece.getSteps().size() > 0)
                    workpiece.removeStep(workpiece.getSteps().size() - 1);
            }

            ((MutateInPlace<Object>) mip).setResult(1, object);

        }

        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.REMOVE;
    }
}
