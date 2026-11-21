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

package com.neep.neepmeat.api.enlightenment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public abstract class TickingEnlightenmentEvent implements EnlightenmentEvent
{
    protected int time;
    protected boolean removed;

    protected final Level world;
    protected final ServerPlayer player;

    protected TickingEnlightenmentEvent(Level world, ServerPlayer player)
    {
        this.world = world;
        this.player = player;
    }

    @Override
    public void spawn()
    {
        EnlightenmentEvent.super.spawn();
    }

    @Override
    public void tick()
    {
        ++time;
    }

    public void markRemoved()
    {
        this.removed = true;
    }

    @Override
    public boolean isRemoved()
    {
        return removed;
    }

//    public static class Factory implements EnlightenmentEvent.Factory
//    {
//        @Override
//        public EnlightenmentEvent create(EnlightenmentEventManager manager, ServerWorld world, ServerPlayerEntity player)
//        {
//            return new TickingEnlightenmentEvent(manager, world, player);
//        }
//
//        @Override
//        public boolean willSpawn(EnlightenmentEventManager manager, ServerWorld world, ServerPlayerEntity player)
//        {
//            return true;
//        }
//    }
}
