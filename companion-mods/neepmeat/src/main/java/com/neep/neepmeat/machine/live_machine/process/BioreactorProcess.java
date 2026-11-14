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

import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.block.entity.StomachBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

public class BioreactorProcess implements Process {
    private static final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.STOMACH,
            LivingMachineComponents.FLUID_INPUT,
            LivingMachineComponents.FLUID_OUTPUT);

    @Override
    public void serverTick(LivingMachineBlockEntity be) {
        be.withComponents(LivingMachineComponents.STOMACH, LivingMachineComponents.FLUID_INPUT, LivingMachineComponents.FLUID_OUTPUT).ifPresent(result -> {
            StomachBlockEntity stomach = (StomachBlockEntity) result.t1().iterator().next();
            stomach.slot().tickRecipe(be.getProgressIncrement(), new Context(be));
        });
    }

    @Override
    public List<ComponentType<?>> getRequired() {
        return REQUIRED;
    }

    @Override
    public Component getName() {
        return NeepMeat.translationKey("process", "bioreactor");
    }

    public record Context(LivingMachineBlockEntity be) {
        public Storage<FluidVariant> fluidInput() {
            return be.getCombinedFluidInput();
        }

        public Storage<FluidVariant> fluidOutput() {
            return be.getCombinedFluidOutput();
        }
    }
}
