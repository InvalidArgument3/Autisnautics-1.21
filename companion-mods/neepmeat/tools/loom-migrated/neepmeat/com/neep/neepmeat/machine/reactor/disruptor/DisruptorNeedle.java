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

package com.neep.neepmeat.machine.reactor.disruptor;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.reactor.block.entity.ReactionCoreBlockEntity;
import org.jetbrains.annotations.Nullable;

public interface DisruptorNeedle extends LivingMachineComponent
{
    @Nullable
    ReactionCoreBlockEntity findCore();

    @Override
    default ComponentType<? extends LivingMachineComponent> getComponentType()
    {
        return LivingMachineComponents.DISRUPTOR_NEEDLE;
    }

    void updateState(boolean active);
}
