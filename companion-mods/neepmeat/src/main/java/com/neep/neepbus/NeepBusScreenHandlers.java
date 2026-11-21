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

package com.neep.neepbus;

import com.neep.neepbus.screen.*;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.world.inventory.MenuType;

import static com.neep.meatlib.screen.ScreenHandlerRegistry.register;
import static com.neep.meatlib.screen.ScreenHandlerRegistry.registerExtended;

public class NeepBusScreenHandlers
{
    public static MenuType<NeepBusConfigScreenHandler> NEEPBUS_CONFIG;
    public static MenuType<NeepBusRangeConfigScreenHandler> NEEPBUS_RANGE_CONFIG;
    public static MenuType<SliderScreenHandler> SLIDER;
    public static MenuType<MovementControllerScreenHandler> MOVEMENT_CONTROL;
    public static MenuType<GaugeScreenHandler> GAUGE;
    public static MenuType<PIDControllerScreenHandler> PID_CONTROLLER;
    public static MenuType<DelayScreenHandler> DELAY;
    public static MenuType<NeepBusBufferScreenHandler> BUFFER;
    public static MenuType<IntegratorScreenHandler> INTEGRATOR;
    public static MenuType<ButtonConfigScreenHandler> BUTTON_CONFIG;
    public static MenuType<MouthScreenHandler> MOUTH;

    public static void init()
    {
        NEEPBUS_CONFIG = registerExtended(NeepMeat.NAMESPACE, "neepbus_config", NeepBusConfigScreenHandler::new);
        NEEPBUS_RANGE_CONFIG = registerExtended(NeepMeat.NAMESPACE, "neepbus_range_config", NeepBusRangeConfigScreenHandler::new);
        SLIDER = register(NeepMeat.NAMESPACE, "neepbus_slider", SliderScreenHandler::new);
        MOVEMENT_CONTROL = registerExtended(NeepMeat.NAMESPACE, "movement_control", MovementControllerScreenHandler::new);
        GAUGE = registerExtended(NeepMeat.NAMESPACE, "neepbus_gauge", GaugeScreenHandler::new);
        PID_CONTROLLER = registerExtended(NeepMeat.NAMESPACE, "pid_controller", PIDControllerScreenHandler::new);
        DELAY = registerExtended(NeepMeat.NAMESPACE, "delay", DelayScreenHandler::new);
        BUFFER = registerExtended(NeepMeat.NAMESPACE, "neepbus_buffer", NeepBusBufferScreenHandler::new);
        INTEGRATOR = registerExtended(NeepMeat.NAMESPACE, "integrator", IntegratorScreenHandler::new);
        BUTTON_CONFIG = registerExtended(NeepMeat.NAMESPACE, "neepbus_button_config", ButtonConfigScreenHandler::new);
        MOUTH = register(NeepMeat.NAMESPACE, "mouth", MouthScreenHandler::new);
    }
}
