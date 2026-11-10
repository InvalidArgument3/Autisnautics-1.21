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
import com.neep.neepmeat.api.plc.robot.*;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.init.NMSounds;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.entity.ImplanterActuator;
import com.neep.neepmeat.plc.block.entity.ImplanterBlockEntity;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.neepmeat.plc.recipe.EntityMutateRecipe;
import com.neep.neepmeat.plc.recipe.ImplantStep;
import com.neep.neepmeat.plc.recipe.PLCRecipes;
import com.neep.neepmeat.plc.robot.RobotMoveToAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ImplantInstruction implements PlcInstruction
{
    private final Supplier<Level> world;
    private final LazyBlockApiCache<MutateInPlace<Entity>, Void> toCache;
    private final Argument from;
    private final Argument to;

    private final GroupedRobotAction group;

    public ImplantInstruction(Supplier<Level> world, List<Argument> arguments)
    {
        this.world = world;
        this.from = arguments.get(0);
        this.to = arguments.get(1);
        this.toCache = LazyBlockApiCache.of(MutateInPlace.ENTITY, to.pos(), world, () -> null);

        group = GroupedRobotAction.of(
                new RobotMoveToAction(from.pos()),
                AtomicAction.of(this::takeFrom),
                new Glue(),
                new SoundAction(world, NMSounds.IMPLANT_INSTRUCTION_APPLY),
                new DelayAction(40),
                AtomicAction.of(this::install)
        );
    }

    public ImplantInstruction(Supplier<Level> world, CompoundTag compound)
    {
        this(world, List.of(
                Argument.fromNbt(compound.getCompound("from")),
                Argument.fromNbt(compound.getCompound("to"))
        ));
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.put("from", from.toNbt());
        nbt.put("to", to.toNbt());
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
        if (plc.getActuator() instanceof ImplanterActuator)
            plc.addRobotAction(group, this::finish);
        else
            throw NeepASM.RuntimeException.notSupported("IMPLANT");
    }

    @Override
    public void cancel(PLC plc)
    {
        group.end(plc);
        plc.controlActuator(actuator ->
        {
            actuator.dumpStored(plc);

            if (actuator instanceof ImplanterBlockEntity implanter)
            {
                implanter.targetRestPos();
            }
        });
    }

    private void takeFrom(PLC plc) throws NeepASM.RuntimeException
    {
        var takenAmount = Instructions.takeItem(from, world, 1, Pattern.compile(".*"));
        if (takenAmount != null)
        {
            plc.getActuator().dumpStored(plc);
            plc.getActuator().setStored(plc, takenAmount);
            var robot = plc.getActuator();
            world.get().playSeededSound(null, robot.getX(), robot.getY(), robot.getZ(), NMSounds.EXTRACT_ITEM, SoundSource.BLOCKS, 1, 1, 1);
        }
        else
        {
            throw new NeepASM.RuntimeException("implant: No item in input storage");
        }
    }

    private void install(PLC plc) throws NeepASM.RuntimeException
    {
        var mip = toCache.find();
        var actuator = plc.getActuator();
        var stored = actuator.getStored(plc);
        if (mip != null && stored != null)
        {
            ImplantStep step = new ImplantStep(stored.resource().getItem(), 1);
            if (install(step, mip))
                stored = null;
        }

        if (stored != null)
            actuator.dumpStored(plc);
        else
            actuator.setStored(plc, null);

        if (actuator instanceof ImplanterBlockEntity implanter)
        {
            implanter.targetRestPos();
        }
    }

    public static boolean install(ImplantStep step, MutateInPlace<Entity> mip)
    {
        Workpiece workpiece;
        Entity entity = mip.get();
        if (entity != null && (workpiece = NMComponents.WORKPIECE.getNullable(entity)) != null)
        {
            // First, ensure that there is at least one valid recipe with the next step
            EntityMutateRecipe partialMatch;
            partialMatch = PLCRecipes.getPartialMatch(PLCRecipes.ENTITY_MANUFACTURE, workpiece, step, mip);
            if (partialMatch == null)
                partialMatch = PLCRecipes.getPartialMatch(PLCRecipes.ENTITY_TO_ITEM, workpiece, step, mip);

            if (partialMatch != null)
            {
                step.mutate(entity);

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

    private void finish(PLC plc)
    {
        plc.advanceCounter();
    }

    public static double getTargetY(Entity target)
    {
        return target.getY() + target.getBbHeight() / 2;
    }

    private void setImplanterTarget(PLC plc, ImplanterActuator implanter) throws NeepASM.RuntimeException
    {
        var mip = toCache.find();
        if (mip != null && mip.get() != null)
        {
            Entity entity = mip.get();
            if (entity != null && NMComponents.WORKPIECE.isProvidedBy(entity))
            {
                Vec3 v = new Vec3(entity.getX(), getTargetY(entity), entity.getZ());
                implanter.setImplantTarget(plc, v, entity);
            }
        }
        else
        {
            implanter.targetRestPos();
            throw new NeepASM.RuntimeException("IMPLANT: No surgery platform with entity found");
        }
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.IMPLANT;
    }

    class Glue implements RobotAction
    {
        @Override
        public boolean finished(PLC plc)
        {
            return plc.getActuator().reachedTarget(plc);
        }

        @Override
        public void start(PLC plc) throws NeepASM.RuntimeException
        {
            plc.controlActuator(ImplanterActuator.class, implanter -> setImplanterTarget(plc, implanter));
        }

        @Override
        public void tick(PLC plc)
        {
        }

        @Override
        public void end(PLC plc)
        {

        }
    }
}
