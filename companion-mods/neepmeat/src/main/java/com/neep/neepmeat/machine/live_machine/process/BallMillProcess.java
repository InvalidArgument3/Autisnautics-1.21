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

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.block.entity.BallMillBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

public class BallMillProcess implements Process {
    @Override
    public void serverTick(LivingMachineBlockEntity be) {
        be.withComponents(LivingMachineComponents.BALL_MILL, LivingMachineComponents.ITEM_INPUT, LivingMachineComponents.ITEM_OUTPUT, LivingMachineComponents.MOTOR_PORT).ifPresent(result -> {
            if (be.getPower() < 0.05f) {
                return;
            }
            for (BallMillBlockEntity mill : result.t1()) {
                mill.tick(be.getProgressIncrement(), be.getCombinedItemInput(), be.getCombinedItemOutput());
            }
        });
    }

    @Override
    public List<ComponentType<?>> getRequired() {
        return List.of(LivingMachineComponents.BALL_MILL, LivingMachineComponents.ITEM_INPUT, LivingMachineComponents.ITEM_OUTPUT, LivingMachineComponents.MOTOR_PORT);
    }

    @Override
    public Component getName() {
        return Component.translatable("process.neepmeat.ball_mill");
    }
}
