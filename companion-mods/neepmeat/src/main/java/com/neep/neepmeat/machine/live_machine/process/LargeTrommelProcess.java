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

import com.neep.meatlib.transfer.CombinedStorage;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.Transaction;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.block.entity.LargeTrommelBlockEntity;
import com.neep.neepmeat.machine.live_machine.component.FluidInputComponent;
import com.neep.neepmeat.machine.live_machine.component.FluidOutputComponent;
import com.neep.neepmeat.machine.live_machine.component.ItemOutputComponent;
import com.neep.neepmeat.machine.small_trommel.TrommelRecipe;
import net.minecraft.network.chat.Component;

import java.util.List;

public class LargeTrommelProcess implements Process {
    public static final float MIN_POWER = 0.05f;

    @Override
    public void serverTick(LivingMachineBlockEntity be) {
        be.withComponents(LivingMachineComponents.LARGE_TROMMEL, LivingMachineComponents.FLUID_INPUT, LivingMachineComponents.FLUID_OUTPUT, LivingMachineComponents.ITEM_OUTPUT, LivingMachineComponents.MOTOR_PORT).ifPresent(result -> {
            if (be.getPower() < MIN_POWER) {
                return;
            }
            List<Storage<FluidVariant>> fluidInputs = result.t2().stream().map(port -> port.getStorage(null)).toList();
            CombinedStorage<FluidVariant, Storage<FluidVariant>> fluidOutput = new CombinedStorage<>(result.t3().stream().map(port -> port.getStorage(null)).toList());
            CombinedStorage<ItemVariant, Storage<ItemVariant>> itemOutput = new CombinedStorage<>(result.t4().stream().map(ItemOutputComponent::getInternalStorage).toList());
            try (Transaction transaction = Transaction.openOuter()) {
                for (LargeTrommelBlockEntity trommel : result.t1()) {
                    LargeTrommelBlockEntity.InputSlot slot = trommel.getStorage();
                    if (slot.isResourceBlank()) {
                        for (Storage<FluidVariant> input : fluidInputs) {
                            for (var view : input.nonEmptyViews()) {
                            com.neep.meatlib.transfer.FluidVariant resource = view.getResource();
                            long moved = input.extract(resource, TrommelRecipe.INPUT_AMOUNT, transaction);
                            long inserted = slot.insert(resource, moved, transaction);
                                if (inserted > 0) {
                                    break;
                                }
                            }
                        }
                    }
                    slot.tick(be.getProgressIncrement() * 2, fluidOutput, itemOutput, transaction);
                }
                transaction.commit();
            }
        });
    }

    @Override
    public List<ComponentType<?>> getRequired() {
        return List.of(LivingMachineComponents.LARGE_TROMMEL, LivingMachineComponents.FLUID_INPUT, LivingMachineComponents.FLUID_OUTPUT, LivingMachineComponents.ITEM_OUTPUT, LivingMachineComponents.MOTOR_PORT);
    }

    @Override
    public Component getName() {
        return Component.translatable("process.neepmeat.trommel");
    }
}
