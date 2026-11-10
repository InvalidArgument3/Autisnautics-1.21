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
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.vm.VM;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Instructions are not ticked. They make changes via {@link com.neep.neepmeat.api.plc.robot.RobotAction} which are
 * executed by the currently selected actuator in the PLC.
 */
public interface PlcInstruction extends Instruction
{
    default boolean canStart(PLC plc) { return true; };

    @Override
    default void start(VM machine) throws NeepASM.RuntimeException
    {
        if (machine instanceof PLC plc)
        {
            start(plc);
        }
        else
        {
            throw new NeepASM.RuntimeException("machine not supported");
        }
    }

    void start(PLC plc) throws NeepASM.RuntimeException;

    /// For cleaning up an intermediate state if the instruction is cancelled externally.
    default void cancel(PLC plc) {};

    default CompoundTag writeNbt(CompoundTag nbt) { return nbt; };

    default void writeVolatileState(CompoundTag nbt) {}

//    static  <T extends Instruction> T copy(Supplier<World> worldSupplier, T t)
//    {
//        return (T) t.getOpcode().createFromNbt(worldSupplier, t.writeNbt(new NbtCompound()));
//    }

    @Override
    @NotNull InstructionProvider getOpcode();

    static CompoundTag writeItem(@Nullable ResourceAmount<ItemVariant> amount)
    {
        if (amount != null)
        {
            var nbt = amount.resource().toNbt();
            nbt.putLong("amount", amount.amount());
            return nbt;
        }
        else return new CompoundTag();
    }

    @Nullable
    static ResourceAmount<ItemVariant> readItem(CompoundTag nbt)
    {
        if (nbt.contains("amount"))
        {
            long amount = nbt.getLong("amount");
            return new ResourceAmount<>(ItemVariant.fromNbt(nbt), amount);
        }
        return null;
    }

    static CompoundTag writeFluid(@Nullable ResourceAmount<FluidVariant> amount)
    {
        if (amount != null)
        {
            var nbt = amount.resource().toNbt();
            nbt.putLong("amount", amount.amount());
            return nbt;
        }
        else return new CompoundTag();
    }

    @Nullable
    static ResourceAmount<FluidVariant> readFluid(CompoundTag nbt)
    {
        if (nbt.contains("amount"))
        {
            long amount = nbt.getLong("amount");
            return new ResourceAmount<>(FluidVariant.fromNbt(nbt), amount);
        }
        return null;
    }
}
