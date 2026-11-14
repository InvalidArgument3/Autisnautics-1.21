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

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@OnlyIn(Dist.CLIENT)
public interface KeyboardEvents
{
    Event<KeyboardEvents> PRE_INPUT = EventFactory.createArrayBacked(
            KeyboardEvents.class,
            (listeners) -> (window, key, scancode, action, modifiers) ->
            {
                boolean override = false;
                for (KeyboardEvents listener : listeners)
                {
                    override = override || listener.onKey(window, key, scancode, action, modifiers);
                }

                return override;
            });

    Event<KeyboardEvents> POST_INPUT = EventFactory.createArrayBacked(
            KeyboardEvents.class,
            (listeners) -> (window, key, scancode, action, modifiers) ->
            {
                boolean override = false;
                for (KeyboardEvents listener : listeners)
                {
                    override = override || listener.onKey(window, key, scancode, action, modifiers);
                }

                return override;
            });


    boolean onKey(long window, int key, int scancode, int action, int modifiers);
}
