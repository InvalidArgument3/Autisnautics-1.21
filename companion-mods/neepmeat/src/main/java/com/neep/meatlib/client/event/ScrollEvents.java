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

package com.neep.meatlib.client.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

public interface ScrollEvents
{
    /**
     * Only invoked when the client player is in a world and has no current screen.
     */
    Event<ScrollEvents> PRE_INGAME_SCROLL = EventFactory.createArrayBacked( ScrollEvents.class,
            (listeners) -> (client, world, player, window, amount) ->
            {
                boolean override = false;
                for (ScrollEvents listener : listeners)
                {
                    override |= listener.onScroll(client, world, player, window, amount);
                }
                return override;
            });

    boolean onScroll(Minecraft client, ClientLevel world, LocalPlayer player, long window, double amount);
}
