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

package com.neep.neepmeat.api.live_machine;

import com.neep.neepmeat.NeepMeat;
import java.util.List;
import net.minecraft.network.chat.Component;

public interface Process
{
    Component NO_PROCESS = NeepMeat.translationKey("screen", "living_machine.no_process");

    void serverTick(LivingMachineBlockEntity be);

    default void onStart(LivingMachineBlockEntity be) { }

    default void onStop(LivingMachineBlockEntity be) { }

    List<ComponentType<?>> getRequired();

    Component getName();

    default List<Component> getProblems(LivingMachineBlockEntity be) { return List.of(); }
}
