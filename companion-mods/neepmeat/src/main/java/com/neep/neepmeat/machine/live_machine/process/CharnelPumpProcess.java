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

import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.Transaction;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.machine.charnel_pump.CharnelPumpBlockEntity;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CharnelPumpProcess implements Process {
    /** Upstream config default, absolute energy. One per-unit is {@link PowerUtil#BASE_POWER}. */
    public static final int MIN_POWER = 500;
    /** Upstream config default, droplets of compressed air per per-unit of power. */
    public static final int POWER_TO_AIR = 100;
    public static final long DISTRIBUTE_AMOUNT = FluidConstants.BOTTLE;
    private static final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.CHARNEL_PUMP,
            LivingMachineComponents.MOTOR_PORT,
            LivingMachineComponents.FLUID_INPUT);

    public static boolean canRun(double puPower) {
        return puPower >= (double) MIN_POWER / PowerUtil.referencePower();
    }

    @Override
    public void serverTick(LivingMachineBlockEntity be) {
        be.withComponents(LivingMachineComponents.CHARNEL_PUMP, LivingMachineComponents.MOTOR_PORT, LivingMachineComponents.FLUID_INPUT).ifPresent(with -> {
            CharnelPumpBlockEntity pump = (CharnelPumpBlockEntity) with.t1().iterator().next();
            try (Transaction transaction = Transaction.openOuter()) {
                pump.serverTick(be.getPower(), be.getCombinedFluidInput(), transaction);
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
        return NeepMeat.translationKey("process", "charnel_pump");
    }
}
