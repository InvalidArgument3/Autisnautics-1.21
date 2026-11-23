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

package com.neep.neepmeat.entity;

import com.google.common.collect.Queues;
import com.neep.neepmeat.network.EntityAnimationS2C;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.world.entity.Entity;
import java.util.Queue;

public interface AnimationSyncable
{
    AnimationQueue getQueue();

    default void syncNearby(String name)
    {
        if (this instanceof Entity entity)
        {
            for (var player : PlayerLookup.tracking(entity))
            {
                EntityAnimationS2C.send(player, entity, name);
            }
        }
    }

    class AnimationQueue
    {
        protected Queue<String> animations = Queues.newArrayDeque();

        public void add(String name)
        {
            animations.add(name);
        }

        public boolean isEmpty()
        {
            return animations.isEmpty();
        }

        public String poll()
        {
            return animations.poll();
        }
    }
}
