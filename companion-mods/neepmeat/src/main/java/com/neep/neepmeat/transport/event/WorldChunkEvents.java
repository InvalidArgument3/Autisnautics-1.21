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

package com.neep.neepmeat.transport.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

@FunctionalInterface
public interface WorldChunkEvents
{
    Event<WorldChunkEvents> LOAD_ENTITIES = EventFactory.createArrayBacked(WorldChunkEvents.class, (listeners) -> ((chunk) ->
    {
        for (var listener : listeners)
        {
            listener.load(chunk);
        }
    }));

    Event<WorldChunkEvents> UNLOAD_ENTITIES = EventFactory.createArrayBacked(WorldChunkEvents.class, listeners -> (chunk) ->
    {
        for (var listener : listeners)
        {
            listener.load(chunk);
        }
    });

    Event<BlockEntityEvent> BE_SET_WORLD = EventFactory.createArrayBacked(BlockEntityEvent.class, listeners -> (chunk, be) ->
    {
        for (var listener : listeners)
        {
            listener.apply(chunk, be);
        }
    });

    Event<BlockEntityEvent> BE_MANUAL_REMOVE = EventFactory.createArrayBacked(BlockEntityEvent.class, listeners -> (chunk, be) ->
    {
        for (var listener : listeners)
        {
            listener.apply(chunk, be);
        }
    });

    void load(LevelChunk chunk);

    @FunctionalInterface
    interface BlockEntityEvent
    {
        void apply(LevelChunk chunk, BlockEntity be);
    }
}
