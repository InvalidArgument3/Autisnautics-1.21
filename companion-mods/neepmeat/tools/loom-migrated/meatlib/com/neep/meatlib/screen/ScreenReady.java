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

package com.neep.meatlib.screen;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.network.GlobalChannelManager;
import net.fabricmc.api.EnvType;
import net.minecraft.resources.ResourceLocation;

public class ScreenReady
{
    public static final GlobalChannelManager<Runnable> SCREEN_READY = GlobalChannelManager.create(
            new ResourceLocation(MeatLib.NAMESPACE, "screen_ready"),
            ChannelFormat.builder(Runnable.class).build());

    // TODO
    private static int readyQueue;

    public static void ready()
    {
        SCREEN_READY.emitter(null).run();
    }

    public static void init()
    {
        SCREEN_READY.receiverHandler(EnvType.SERVER, (player, buf, responseSender) ->
        {
            if (player.containerMenu instanceof ScreenReadyListener listener)
                listener.onScreenReady();
            else
                readyQueue++;

            return () -> {};
        });
    }
}
