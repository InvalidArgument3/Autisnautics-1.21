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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Instructions are not ticked. They make changes via {@link com.neep.neepmeat.api.plc.robot.RobotAction} which are
 * executed by the currently selected actuator in the PLC.
 */
public interface PlcInstruction extends Instruction
{
    default boolean canUse(PLC plc) { return true; };

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
//        return (T) t.getOpcode().createFromNbt(worldSupplier, t.writeNbt(new CompoundTag()));
//    }

    @Override
    @NotNull InstructionProvider getOpcode();

    static CompoundTag writeItem(@Nullable ItemStack stack)
    {
        CompoundTag nbt = new CompoundTag();
        if (stack != null && !stack.isEmpty())
        {
            nbt.putString("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            nbt.putInt("count", stack.getCount());
        }
        return nbt;
    }

    static ItemStack readItem(CompoundTag nbt)
    {
        if (!nbt.contains("item"))
            return ItemStack.EMPTY;
        var item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(nbt.getString("item")));
        return item.map(value -> new ItemStack(value, nbt.getInt("count"))).orElse(ItemStack.EMPTY);
    }

    static CompoundTag writeFluid(@Nullable FluidStack stack)
    {
        CompoundTag nbt = new CompoundTag();
        if (stack != null && !stack.isEmpty())
        {
            nbt.putString("fluid", BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString());
            nbt.putInt("amount", stack.getAmount());
        }
        return nbt;
    }

    static FluidStack readFluid(CompoundTag nbt)
    {
        if (!nbt.contains("fluid"))
            return FluidStack.EMPTY;
        Fluid fluid = BuiltInRegistries.FLUID.getOptional(ResourceLocation.parse(nbt.getString("fluid"))).orElse(null);
        if (fluid == null)
            return FluidStack.EMPTY;
        return new FluidStack(fluid, nbt.getInt("amount"));
    }
}
