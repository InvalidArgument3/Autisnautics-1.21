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

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.plc.robot.AtomicAction;
import com.neep.neepmeat.api.plc.robot.GroupedRobotAction;
import com.neep.neepmeat.api.plc.robot.SoundAction;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.item.ItemImplantItem;
import com.neep.neepmeat.network.ParticleSpawnS2C;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.entity.CombineActuator;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.neepmeat.plc.recipe.AbstractItemManufactureRecipe;
import com.neep.neepmeat.plc.recipe.CombineStep;
import com.neep.neepmeat.plc.recipe.PLCRecipes;
import com.neep.neepmeat.plc.robot.RobotMoveToAction;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageUtil;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class CombineInstruction implements PlcInstruction
{
    private final Supplier<Level> worldSupplier;
    protected Argument from;
    protected Argument to;
    private final GroupedRobotAction group;

    public CombineInstruction(Supplier<Level> world, List<Argument> arguments)
    {
        this.worldSupplier = world;
        this.from = arguments.get(0);
        this.to = arguments.get(1);

        group = GroupedRobotAction.of(
                new RobotMoveToAction(from.pos()),
                new SoundAction(world, SoundEvents.BEEHIVE_EXIT),
                AtomicAction.of(this::takeFirst),
                new RobotMoveToAction(to.pos()),
                AtomicAction.of(this::complete)
        );
    }

    public CombineInstruction(Supplier<Level> world, CompoundTag nbt)
    {
        this(world, List.of(
                Argument.fromNbt(nbt.getCompound("from")),
                Argument.fromNbt(nbt.getCompound("to"))
        ));
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        group.readNbt(plc.volatileStorage());
        if (plc.getActuator() instanceof CombineActuator)
            plc.addRobotAction(group, this::finish);
        else
            throw NeepASM.RuntimeException.notSupported("COMBINE");
    }

    @Override
    public void cancel(PLC plc)
    {
        plc.controlActuator(a -> a.dumpStored(plc));
        group.end(plc);
    }

    private void takeFirst(PLC plc) throws NeepASM.RuntimeException
    {
        var stored = takeItem(LazyBlockApiCache.itemSided(from, worldSupplier));
        if (stored == null)
        {
            throw new NeepASM.RuntimeException("COMBINE: no item found");
        }
        else
        {
            plc.getActuator().setStored(plc, stored);
            if (worldSupplier.get() instanceof ServerLevel serverWorld)
            {
                ParticleSpawnS2C.sendNearby(serverWorld, from.pos(), new ItemParticleOption(ParticleTypes.ITEM, stored.resource().toStack()),
                        Vec3.atCenterOf(from.pos()), new Vec3(0, 0.3, 0), new Vec3(0.1, 0.1, 0.1), 4);
            }
        }
    }

    private void complete(PLC plc) throws NeepASM.RuntimeException
    {
        var actuator = plc.getActuator();
        final var stored = actuator.getStored(plc);
        var mip = MutateInPlace.ITEM.find(worldSupplier.get(), to.pos(), null);
        if (stored != null && mip != null && mip.get() != null)
        {
            ItemStack stack = mip.get();

            var step = CombineStep.get(stored.resource().toStack((int) stored.amount()));

            // TODO: This is a teMpORaRy measure because I can't be bothered to add ANOTHER recipe type
            if (stored.resource().getObject() instanceof ItemImplantItem item)
            {
                boolean installed = item.install(stack, s -> actuator.spawnItem(MeatlibStorageUtil.amountFromStack(s)));
                if (installed)
                {
                    actuator.setStored(plc, null);
                    mip.setResult(1, stack);

                    playCompleteSound(plc);
                }
                else
                {
                    actuator.dumpStored(plc);
                }
            }

            var workpiece = NMComponents.WORKPIECE.maybeGet(stack).orElse(null);

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

                playCompleteSound(plc);

                actuator.setStored(plc, null);

                return;
            }
        }

        actuator.dumpStored(plc);
    }

    private void playCompleteSound(PLC plc) throws NeepASM.RuntimeException
    {
        if (worldSupplier.get() instanceof ServerLevel serverWorld)
        {
            var robot = plc.getActuator();
            serverWorld.playSeededSound(null, robot.getX(), robot.getY(), robot.getZ(), NMSounds.COMBINE_INSTRUCTION_APPLY.get(), SoundSource.NEUTRAL, 1, 1, 1);
        }
    }

    private ResourceAmount<ItemVariant> takeItem(LazyBlockApiCache<Storage<ItemVariant>, Direction> target)
    {
        var storage = target.find();
        if (storage != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                ResourceAmount<ItemVariant> found = StorageUtil.findExtractableContent(storage, transaction);
                if (found != null)
                {
                    long extracted = storage.extract(found.resource(), 1, transaction);
                    if (extracted > 0)
                    {
                        var res = new ResourceAmount<>(found.resource(), extracted);
                        transaction.commit();
                        return res;
                    }

                    transaction.abort();
                }
            }
        }

        return null;
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.COMBINE;
    }

    void finish(PLC plc)
    {
        plc.advanceCounter();
//        System.out.println("Finish");
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
}
