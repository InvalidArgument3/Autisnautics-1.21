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

import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.plc.recipe.Workpiece;
import com.neep.neepmeat.api.plc.robot.AtomicAction;
import com.neep.neepmeat.api.plc.robot.DelayAction;
import com.neep.neepmeat.api.plc.robot.GroupedRobotAction;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.init.NMSounds;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.entity.CombineActuator;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.neepmeat.plc.recipe.*;
import com.neep.neepmeat.plc.robot.RobotMoveToAction;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class InjectInstruction implements PlcInstruction
{
    private final Supplier<Level> world;
    private final Argument from;
    private final Argument to;
    private ResourceAmount<FluidVariant> stored; // TODO: save

    private final GroupedRobotAction group;

    public InjectInstruction(Supplier<Level> world, List<Argument> arguments)
    {
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

    private void playTakeSound(PLC plc) throws NeepASM.RuntimeException
    {
        var robot = plc.getActuator();
        world.get().playSeededSound(null, robot.getX(), robot.getY(), robot.getZ(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1, 1, 1);
    }

    private void playSound(PLC plc) throws NeepASM.RuntimeException
    {
        var robot = plc.getActuator();
        world.get().playSeededSound(null, robot.getX(), robot.getY(), robot.getZ(), NMSounds.INJECT_INSTRUCTION_APPLY, SoundSource.BLOCKS, 1, 1, 1);
    }

    public InjectInstruction(Supplier<Level> world, CompoundTag nbt)
    {
        this(world, List.of(
                Argument.fromNbt(nbt.getCompound("from")),
                Argument.fromNbt(nbt.getCompound("to"))
        ));
        this.stored = PlcInstruction.readFluid(nbt.getCompound("stored"));
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.put("from", from.toNbt());
        nbt.put("to", to.toNbt());
        nbt.put("stored", PlcInstruction.writeFluid(stored));

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
            throw NeepASM.RuntimeException.notSupported("INJECT");
    }

    @Override
    public void cancel(PLC plc)
    {
        group.end(plc);
        // Can't really drop a fluid.
    }

    private void takeFrom(PLC plc) throws NeepASM.RuntimeException
    {
        // TODO: control amount somehow
        var takenAmount = Instructions.takeFluid(from, world, FluidConstants.BUCKET);
        if (takenAmount != null)
        {
            this.stored = takenAmount;
        }
        else
        {
            throw new NeepASM.RuntimeException("Insufficient or no fluid at " + from);
        }
    }

    private boolean insertToItem(MutateInPlace<ItemStack> mip, InjectStep step)
    {
        ItemStack stack = mip.get();

        if (stack != null)
        {
            Workpiece workpiece = NMComponents.WORKPIECE.getNullable(stack);
            if (workpiece != null && PLCRecipes.isValidStep(PLCRecipes.MANUFACTURE, workpiece, step, stack.getItem()))
            {
                workpiece.addStep(step);

                mip.setResult(1, stack);

                AbstractItemManufactureRecipe recipe = MeatlibRecipes.getInstance().getFirstMatch(PLCRecipes.MANUFACTURE, mip).orElse(null);
                if (recipe != null)
                {
                    recipe.ejectOutputs(mip, null);
                    workpiece.clearSteps();
                }
                else
                {
                    // Special handling for transforming tools.
                    TransformingToolRecipe transformingToolRecipe = TransformingToolRecipe.getInstance();
                    if (transformingToolRecipe.matches(mip))
                    {
                        transformingToolRecipe.ejectOutputs(mip, null);
                        workpiece.clearSteps();
                    }
                }

                return true;
            }
        }

        return false;
    }

    private void returnFluid()
    {
        if (stored != null)
        {
            Instructions.giveFluid(from, world, stored);
            stored = null;
        }
    }

    private boolean insertToEntity(MutateInPlace<Entity> mip, InjectStep step)
    {
        Entity entity = mip.get();

        if (entity != null)
        {
            Workpiece workpiece = NMComponents.WORKPIECE.getNullable(entity);
            if (workpiece != null)
            {
                workpiece.addStep(step);

                mip.setResult(1, entity);

                EntityMutateRecipe recipe = PLCRecipes.matchEntityMutate(mip);

                if (recipe != null)
                {
                    recipe.ejectOutputs(mip, null);
                    workpiece.clearSteps();
                }

                return true;
            }
        }

        return false;
    }

    private void insert(PLC plc)
    {
        if (stored != null)
        {
            var step = new InjectStep(stored.resource());
            var itemMip = MutateInPlace.ITEM.find(world.get(), to.pos(), null);
            if (itemMip != null)
            {
                if (insertToItem(itemMip, step))
                {
                    stored = null;
                }
                return;
            }

            var entityMip = MutateInPlace.ENTITY.find(world.get(), to.pos(), null);
            if (entityMip != null)
            {
                if (insertToEntity(entityMip, step))
                {
                    stored = null;
                }

                return;
            }
        }
    }

    private void finish(PLC plc)
    {
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.INJECT;
    }

    private class Glue extends RobotMoveToAction
    {
        public Glue(BlockPos target)
        {
            super(target);
        }

        @Override
        public void start(PLC plc) throws NeepASM.RuntimeException
        {
            super.start(plc);

            var itemMip = MutateInPlace.ITEM.find(world.get(), to.pos(), null);
            if (itemMip != null)
            {
                return;
            }

            var entityMip = MutateInPlace.ENTITY.find(world.get(), to.pos(), null);
            if (entityMip != null && entityMip.get() != null)
            {
                return;
            }

            returnFluid();
            throw new NeepASM.RuntimeException("INJECT: No item or entity");
        }
    }
}
