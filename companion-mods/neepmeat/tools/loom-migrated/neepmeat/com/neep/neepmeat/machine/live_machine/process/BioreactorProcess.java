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

package com.neep.neepmeat.machine.live_machine.process;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.network.chat.Component;
import java.util.List;

public class BioreactorProcess implements Process
{
    private static final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.STOMACH,
            LivingMachineComponents.FLUID_INPUT,
            LivingMachineComponents.FLUID_OUTPUT
    );

    @Override
    public void serverTick(LivingMachineBlockEntity be)
    {
        be.withComponents(LivingMachineComponents.STOMACH, LivingMachineComponents.FLUID_INPUT, LivingMachineComponents.FLUID_OUTPUT).ifPresent(r ->
        {
            float progressIncrement = be.getProgressIncrement();

            var stomachs = r.t1();
            var stomach = stomachs.iterator().next();

            stomach.getSlot().tickRecipe(progressIncrement, new Context(be));
        });
    }

    @Override
    public List<ComponentType<?>> getRequired()
    {
        return REQUIRED;
    }

    @Override
    public Component getName()
    {
        return NeepMeat.translationKey("process", "bioreactor");
    }

    public record Context(LivingMachineBlockEntity be)
    {
        public Storage<ItemVariant> itemInput()
        {
            return be.getCombinedItemInput();
        }

        public Storage<FluidVariant> fluidInput()
        {
            return be.getCombinedFluidInput();
        }

        public Storage<FluidVariant> fluidOutput()
        {
            return be.getCombinedFluidOutput();
        }
    }
}
