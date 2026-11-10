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

package com.neep.neepmeat.client;

import com.neep.meatlib.util.ClientComponents;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMEntities;
import com.neep.neepmeat.machine.advanced_integrator.AdvancedIntegratorClientComponent;
import com.neep.neepmeat.machine.charnel_pump.CharnelPumpClient;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.phage_ray.PhageRayClientComponent;
import com.neep.neepmeat.machine.pylon.PylonClientComponent;

public class NMClientComponents
{
    public static void init()
    {
        ClientComponents.register(LivingMachines.CHARNEL_PUMP_BE, CharnelPumpClient::new);
        ClientComponents.register(NMBlockEntities.ADVANCED_INTEGRATOR, AdvancedIntegratorClientComponent::new);
        ClientComponents.register(NMBlockEntities.PYLON, PylonClientComponent::new);
        ClientComponents.register(NMEntities.PHAGE_RAY, PhageRayClientComponent::new);
    }
}
