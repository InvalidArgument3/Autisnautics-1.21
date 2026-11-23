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

package com.neep.neepmeat.enlightenment.event;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.enlightenment.TickingEnlightenmentEvent;
import com.neep.neepmeat.enlightenment.EnlightenmentManager;
import com.neep.neepmeat.enlightenment.PlayerEnlightenmentManager;
import com.neep.neepmeat.init.NMEnlightenmentEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RandomClockEnlightenmentEvent extends TickingEnlightenmentEvent
{
    private final int maxTime;
    private final PlayerEnlightenmentManager manager;

    public RandomClockEnlightenmentEvent(Level world, ServerPlayer player)
    {
        super(world, player);
        // Up to 7 minutes
        maxTime = (int) (Math.random() * 7 * 60 * 20);
        manager = EnlightenmentManager.get(player);
    }

    @Override
    public @Nullable Factory getType()
    {
        return NMEnlightenmentEvents.RANDOM_CLOCK;
    }

    @Override
    public void spawn()
    {
        super.spawn();
        manager.randomClock = true;
        manager.sync();
    }

    @Override
    public void tick()
    {
        super.tick();
        if (time > maxTime)
        {
            markRemoved();
            manager.randomClock = false;
            manager.sync();
        }
    }
}
