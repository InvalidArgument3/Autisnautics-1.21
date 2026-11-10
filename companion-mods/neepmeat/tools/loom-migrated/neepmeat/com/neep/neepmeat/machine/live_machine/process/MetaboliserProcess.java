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
import com.neep.neepmeat.api.processing.MeatFluidUtil;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.power_flower.PowerFlowerControllerBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.network.chat.Component;
import java.util.List;

public class MetaboliserProcess implements Process
{
    private static final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.STOMACH,
            LivingMachineComponents.METABOLISER_SEGMENT
    );

    public static PowerFlowerControllerBlockEntity.FoodEnergyEntry foodEnergy(FluidVariant fluid)
    {
        if (fluid.isOf(NMFluids.MEAT.still()))
            return new PowerFlowerControllerBlockEntity.FoodEnergyEntry(40, 1);

        if (fluid.isOf(NMFluids.ANIMAL_FEED.still()))
            return new PowerFlowerControllerBlockEntity.FoodEnergyEntry(60, 1);

        if (fluid.isOf(NMFluids.FOOD.still()))
        {
            return new PowerFlowerControllerBlockEntity.FoodEnergyEntry(1 + 9 * (int) Math.ceil(MeatFluidUtil.getHunger(fluid)), 1);
        }

        return PowerFlowerControllerBlockEntity.NONE;
    }

    @Override
    public void serverTick(LivingMachineBlockEntity be)
    {
        be.withComponents(LivingMachineComponents.STOMACH, LivingMachineComponents.METABOLISER_SEGMENT).ifPresent(result ->
        {
            var segments = result.t2();

            int max = NeepMeat.CONFIG.metaboliser.maxSegments.getRealValue();
            if (segments.size() > max)
            {
                return;
            }

            long energyProduced = 0;
            long toConsume = segments.size() * 4L;
            try (Transaction transaction = Transaction.openOuter())
            {
                for (var view : be.getCombinedFluidInput().nonEmptyViews())
                {
                    var resource = view.getResource();
                    var entry = foodEnergy(view.getResource());
                    if (entry != PowerFlowerControllerBlockEntity.NONE)
                    {
                        long adjusted = toConsume * entry.consume();
                        long extracted = view.extract(resource, adjusted, transaction);
                        extracted /= entry.consume();

                        toConsume -= extracted;
//                        long produceAmount = PowerUtils.absToAmount(NMFluids.STILL_ETHEREAL_FUEL, entry.energy());
//                        be.getCombinedFluidOutput().insert(FluidVariant.of(NMFluids.STILL_ETHEREAL_FUEL), produceAmount, transaction);
                        energyProduced += entry.energy() * extracted;
                    }
                }

                for (var segment : segments)
                {
                    segment.setOutputEnergy((long) ((energyProduced * 1.5) / segments.size()));
                }

                transaction.commit();
            }
        });
    }

    @Override
    public List<Component> getProblems(LivingMachineBlockEntity be)
    {
        return be.withComponents(LivingMachineComponents.METABOLISER_SEGMENT).map(result ->
        {
            int max = NeepMeat.CONFIG.metaboliser.maxSegments.getRealValue();
            List<Component> problems = new ObjectArrayList<>();
            if (result.t1().size() > max)
                problems.add(Component.nullToEmpty("Too many segments (>%s)".formatted(max)));
            return problems;
        }).orElse(List.of());
    }

    @Override
    public List<ComponentType<?>> getRequired()
    {
        return REQUIRED;
    }

    @Override
    public Component getName()
    {
        return NeepMeat.translationKey("process", "metaboliser");
    }
}
