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

import com.neep.meatlib.MeatLib;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.machine.charnel_pump.CharnelPumpBlockEntity;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.network.chat.Component;
import java.util.List;

public class CharnelPumpProcess implements Process
{
    public static final long DISTRIBUTE_AMOUNT = FluidConstants.BOTTLE;
    private static final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.CHARNEL_PUMP,
            LivingMachineComponents.MOTOR_PORT,
            LivingMachineComponents.FLUID_INPUT
    );

    public static boolean canRun(double puPower, CharnelPumpBlockEntity be)
    {
        if (MeatLib.vsUtil == null) {
            return puPower >= (double) NeepMeat.CONFIG.charnelPump.minPower.getRealValue() / PowerUtil.referencePower();
        }
        return puPower >= (double) NeepMeat.CONFIG.charnelPump.minPower.getRealValue() / PowerUtil.referencePower() && !MeatLib.vsUtil.hasShipAtPosition(be.getBlockPos(),be.getLevel());

    }

    @Override
    public void serverTick(LivingMachineBlockEntity be)
    {
        be.withComponents(LivingMachineComponents.CHARNEL_PUMP, LivingMachineComponents.MOTOR_PORT, LivingMachineComponents.FLUID_INPUT).ifPresent(with ->
        {
            CharnelPumpBlockEntity pump = with.t1().iterator().next();

            try (Transaction transaction = Transaction.openOuter())
            {
                Storage<FluidVariant> inputStorage = be.getCombinedFluidInput();
                pump.serverTick(be.getPower(), inputStorage, transaction);
                transaction.commit();;
            }
        });
    }

    @Override
    public List<Component> getProblems(LivingMachineBlockEntity be)
    {
        return be.withComponents(LivingMachineComponents.CHARNEL_PUMP, LivingMachineComponents.MOTOR_PORT, LivingMachineComponents.FLUID_INPUT).<List<Component>>map(with ->
        {
            List<Component> problems = new ObjectArrayList<>();

            if (!canRun(be.getPower(), with.t1().iterator().next()))
                problems.add(Component.nullToEmpty("Power < " + PowerUtil.formatPower(NeepMeat.CONFIG.charnelPump.minPower.getRealValue(), 0)));

            try (Transaction transaction = Transaction.openOuter())
            {
                long droplets = Math.max(10, (long) (be.getPower() * NeepMeat.CONFIG.charnelPump.powerToAir.getRealValue()));
                if (droplets != be.getCombinedFluidInput().extract(FluidVariant.of(NMFluids.COMPRESSED_AIR.still()), droplets, transaction))
                    problems.add(Component.nullToEmpty("No compressed air"));

                if (DISTRIBUTE_AMOUNT != be.getCombinedFluidInput().extract(NMFluids.WORK_FLUID.variant(), DISTRIBUTE_AMOUNT, transaction))
                    problems.add(Component.nullToEmpty("Integration fluid < " + DISTRIBUTE_AMOUNT + "d"));

                transaction.abort();
            }

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
        return NeepMeat.translationKey("process", "charnel_pump");
    }
}
