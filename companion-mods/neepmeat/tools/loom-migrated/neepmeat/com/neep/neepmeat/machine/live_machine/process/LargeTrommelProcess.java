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
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.component.ItemOutputComponent;
import com.neep.neepmeat.machine.small_trommel.TrommelRecipe;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.stream.StreamSupport;

public class LargeTrommelProcess implements Process
{
    public static final float MIN_POWER = 0.05f;

    @Override
    public void serverTick(LivingMachineBlockEntity be)
    {
        be.withComponents(
                LivingMachineComponents.LARGE_TROMMEL,
                LivingMachineComponents.FLUID_INPUT,
                LivingMachineComponents.FLUID_OUTPUT,
                LivingMachineComponents.ITEM_OUTPUT,
                LivingMachineComponents.MOTOR_PORT).ifPresent(result ->
        {
            var trommels = result.t1();
            var fluidInputs = result.t2();
            var fluidOutputs = result.t3();
            var itemOutputs = result.t4();

            if (be.getPower() < MIN_POWER)
            {
                return;
            }

            float progressIncrement = be.getProgressIncrement() * 2;

            try (Transaction transaction = Transaction.openOuter())
            {
                for (var trommel : trommels)
                {
                    var slot = trommel.getStorage();

                    if (slot.isResourceBlank())
                    {
                        // Move fluid from an input slot into the trommel block entity
                        try (Transaction inner = transaction.openNested())
                        {
                            List<StorageView<FluidVariant>> inputViews = fluidInputs.stream()
                                    .map(c -> c.getStorage(null))
                                    .flatMap(s -> StreamSupport.stream(s.nonEmptyViews().spliterator(), false))
                                    .toList();

                            if (!inputViews.isEmpty())
                            {
                                StorageView<FluidVariant> input = inputViews.get(be.getRandom().nextInt(inputViews.size()));
                                // Transfer
                                FluidVariant variant = input.getResource();
                                long extracted = input.extract(variant, TrommelRecipe.INPUT_AMOUNT, inner);
                                long inserted = slot.insert(variant, extracted, inner);
                                if (inserted == TrommelRecipe.INPUT_AMOUNT && slot.getAmount() == TrommelRecipe.INPUT_AMOUNT)
                                    inner.commit();
                            }
                        }
                    }

                    if (!slot.isResourceBlank())
                    {
                        CombinedStorage<FluidVariant, Storage<FluidVariant>> fluidOutput = new CombinedStorage<>(fluidOutputs.stream().map(c -> c.getStorage(null)).toList());
                        CombinedStorage<ItemVariant, Storage<ItemVariant>> itemOutput = new CombinedStorage<>(itemOutputs.stream().map(ItemOutputComponent::getInternalStorage).toList());
                        slot.tick(progressIncrement, fluidOutput, itemOutput, transaction);
                    }
                }
                transaction.commit();
            }
        });
    }

    @Override
    public List<Component> getProblems(LivingMachineBlockEntity be)
    {
        List<Component> problems = new ObjectArrayList<>();
        if (be.getPower() < MIN_POWER)
        {
            problems.add(Component.nullToEmpty("Power < " + PowerUtil.formatEnergy((long) (MIN_POWER * PowerUtil.BASE_POWER), 0)));
        }
        return problems;
    }

    @Override
    public List<ComponentType<?>> getRequired()
    {
        return List.of(
                LivingMachineComponents.LARGE_TROMMEL,
                LivingMachineComponents.FLUID_INPUT,
                LivingMachineComponents.FLUID_OUTPUT,
                LivingMachineComponents.ITEM_OUTPUT,
                LivingMachineComponents.MOTOR_PORT
        );
    }

    @Override
    public Component getName()
    {
        return NeepMeat.translationKey("process", "trommel");
    }
}
