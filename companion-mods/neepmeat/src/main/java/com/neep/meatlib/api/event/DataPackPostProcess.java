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

import com.neep.meatlib.MeatLib;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.util.Set;

@FunctionalInterface
public interface DataPackPostProcess
{
    ResourceLocation FIRST = ResourceLocation.fromNamespaceAndPath(MeatLib.NAMESPACE, "first");
    ResourceLocation ORE_FAT = ResourceLocation.fromNamespaceAndPath(MeatLib.NAMESPACE, "ore_fat");
    ResourceLocation END = ResourceLocation.fromNamespaceAndPath(MeatLib.NAMESPACE, "second");

    Event<DataPackPostProcess> AFTER_DATA_PACK_LOAD = EventFactory.createWithPhases(DataPackPostProcess.class,
            (listeners) -> (server ->
            {
                for (var listener : listeners)
                {
                    listener.event(server);
                }
            }),
            FIRST, ORE_FAT, Event.DEFAULT_PHASE, END);

    Event<SyncToPlayers> SYNC = EventFactory.createArrayBacked(SyncToPlayers.class,
            (listeners) -> ((server, players) ->
            {
                for (var listener : listeners)
                {
                    listener.sync(server, players);
                }
            }));

    void event(MinecraftServer server);

    @FunctionalInterface
    interface SyncToPlayers
    {
        void sync(MinecraftServer server, Set<ServerPlayer> players);
    }
}
