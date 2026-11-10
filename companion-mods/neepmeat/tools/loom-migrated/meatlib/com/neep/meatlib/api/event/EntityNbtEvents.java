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

package com.neep.meatlib.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public interface EntityNbtEvents
{
    Event<EntityNbtEvents> WRITE = EventFactory.createArrayBacked(EntityNbtEvents.class,
            (listeners) -> ((entity, nbt) ->
            {
                for (EntityNbtEvents listener : listeners)
                {
                    listener.writeNbt(entity, nbt);
                }
            }));

    Event<EntityNbtEvents> READ = EventFactory.createArrayBacked(EntityNbtEvents.class,
            (listeners) -> ((entity, nbt) ->
            {
                for (EntityNbtEvents listener : listeners)
                {
                    listener.writeNbt(entity, nbt);
                }
            }));

    void writeNbt(Entity entity, CompoundTag nbt);
}
