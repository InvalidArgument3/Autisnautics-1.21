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
 *
 * Dynamic ore-fat fluids are not generated from smelting recipes. Raw iron and blood become meat,
 * with the upstream compressed-air discount on the blood cost.
 */

package com.neep.neepmeat.machine.live_machine.process;

import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Transaction;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.fluid.BloodFluids;
import com.neep.neepmeat.fluid.CompressedAir;
import com.neep.neepmeat.fluid.MeatFluid;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import com.neep.meatlib.transfer.ItemVariant;

import java.util.List;

public class PressurisedDigesterProcess implements Process {
    public static final long AIR = 90;
    public static final long BLOOD = FluidConstants.INGOT;
    private static final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.STOMACH,
            LivingMachineComponents.FLUID_OUTPUT,
            LivingMachineComponents.ITEM_INPUT);

    @Override
    public void serverTick(LivingMachineBlockEntity be) {
        if (be.getLevel() == null || be.getLevel().getGameTime() % 5 != 0) {
            return;
        }
        be.withComponents(LivingMachineComponents.STOMACH).ifPresent(result -> {
            try (Transaction transaction = Transaction.openOuter()) {
                float discount = 0f;
                try (Transaction air = transaction.openNested()) {
                    if (be.getCombinedFluidInput().extract(FluidVariant.of(CompressedAir.STILL.get()), AIR, air) >= AIR) {
                        discount = 0.5f;
                        air.commit();
                    }
                }
                long blood = (long) (BLOOD * (1f - discount));
                try (Transaction inner = transaction.openNested()) {
                    boolean item = be.getCombinedItemInput().extract(ItemVariant.of(Items.RAW_IRON), 1, inner) == 1;
                    boolean fluid = be.getCombinedFluidInput().extract(FluidVariant.of(BloodFluids.BLOOD.get()), blood, inner) == blood;
                    boolean out = be.getCombinedFluidOutput().insert(FluidVariant.of(MeatFluid.STILL.get()), FluidConstants.INGOT, inner) == FluidConstants.INGOT;
                    if (item && fluid && out) {
                        inner.commit();
                    }
                }
                transaction.commit();
            }
        });
    }

    @Override
    public List<ComponentType<?>> getRequired() {
        return REQUIRED;
    }

    @Override
    public Component getName() {
        return NeepMeat.translationKey("process", "pressurised_digester");
    }
}
