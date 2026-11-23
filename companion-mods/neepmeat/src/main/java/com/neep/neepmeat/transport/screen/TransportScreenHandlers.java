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

package com.neep.neepmeat.transport.screen;

import com.neep.meatlib.screen.ScreenHandlerRegistry;
import com.neep.neepmeat.NeepMeat;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.world.inventory.MenuType;

public class TransportScreenHandlers
{
    public static MenuType<LimiterValveScreenHandler> LIMITER_VALVE;
    public static MenuType<ManualRequesterScreenHandler> ITEM_REQUESTER_HANDLER;
    public static MenuType<VSCScreenHandler> VSC;
    public static MenuType<PowerEmitterScreenHandler> POWER_EMITTER;
    public static MenuType<PipePriorityScreenHandler> PIPE_PRIORITY;
    public static MenuType<AdvancedEjectorScreenHandler> ADVANCED_EJECTOR;
    public static MenuType<PipeDriverScreenHandler> PIPE_DRIVER;
    public static MenuType<RequesterScreenHandler> REQUESTER;
    public static MenuType<ProcessInterfaceScreenHandler> ITEM_REQUEST_LISTENER;
    public static MenuType<ProcessCardScreenHandler> PROCESS_CARD;

    public static void registerScreenHandlers()
    {
        LIMITER_VALVE = ScreenHandlerRegistry.register(NeepMeat.NAMESPACE, "limiter_valve", LimiterValveScreenHandler::new);
        VSC = ScreenHandlerRegistry.register(NeepMeat.NAMESPACE, "vsc", VSCScreenHandler::new);
        PIPE_PRIORITY = ScreenHandlerRegistry.register(NeepMeat.NAMESPACE, "pipe_priority", PipePriorityScreenHandler::new);
        ADVANCED_EJECTOR = ScreenHandlerRegistry.register(NeepMeat.NAMESPACE, "advanced_ejector", AdvancedEjectorScreenHandler::new);
        POWER_EMITTER = ScreenHandlerRegistry.register(NeepMeat.NAMESPACE, "power_emitter", PowerEmitterScreenHandler::new);
        ITEM_REQUESTER_HANDLER = ScreenHandlerRegistry.register(NeepMeat.NAMESPACE, "item_requester", ManualRequesterScreenHandler::new);
        PIPE_DRIVER = ScreenHandlerRegistry.register(NeepMeat.NAMESPACE, "pipe_driver", PipeDriverScreenHandler::new);
        REQUESTER = ScreenHandlerRegistry.register(NeepMeat.NAMESPACE, "requester", RequesterScreenHandler::new);
        ITEM_REQUEST_LISTENER = ScreenHandlerRegistry.register(NeepMeat.NAMESPACE, "item_request_listener", ProcessInterfaceScreenHandler::new);
        PROCESS_CARD = ScreenHandlerRegistry.register(NeepMeat.NAMESPACE, "process_card", ProcessCardScreenHandler::new);
    }
}
