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

package com.neep.meatlib.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

//@Cursed
public class MinecraftServerAccess
{
    @Nullable private static MinecraftServer SERVER;

    public static void init()
    {
        ServerLifecycleEvents.SERVER_STARTING.register(server ->
                SERVER = server);

        ServerLifecycleEvents.SERVER_STOPPED.register(server ->
                SERVER = null);
    }

    @Nullable
    public static MinecraftServer get()
    {
        return SERVER;
    }
}
