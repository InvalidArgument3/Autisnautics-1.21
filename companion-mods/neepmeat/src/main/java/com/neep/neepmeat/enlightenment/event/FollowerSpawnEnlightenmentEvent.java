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

import com.neep.neepbus.client.screen.RangeConfigWidget;
import com.neep.neepmeat.api.enlightenment.EnlightenmentEvent;
import com.neep.neepmeat.entity.follower.FollowerEntity;
import com.neep.neepmeat.init.NMEntities;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class FollowerSpawnEnlightenmentEvent implements EnlightenmentEvent
{
    private final Level world;
    private final ServerPlayer player;

    public FollowerSpawnEnlightenmentEvent(Level world, ServerPlayer player)
    {
        this.world = world;
        this.player = player;
    }

    @Override
    public void spawn()
    {
        FollowerEntity entity = new FollowerEntity(NMEntities.FOLLOWER, world);
        entity.setPosRaw(player.getX(), player.getY(), player.getZ());
        entity.setIterations(ThreadLocalRandom.current().nextInt(5));
        entity.teleportRandomly(player.position());
        entity.setPersistentTarget(player);
        world.addFreshEntity(entity);
    }

    @Override
    public void tick()
    {

    }

    @Override
    public boolean isRemoved()
    {
        return true;
    }
}
