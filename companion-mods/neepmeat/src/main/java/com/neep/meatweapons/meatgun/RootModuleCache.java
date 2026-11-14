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

package com.neep.meatweapons.meatgun;

import com.neep.meatweapons.item.meatgun.Meatgun;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import com.google.common.cache.CacheBuilder;

import java.util.Map;
import java.util.UUID;

public class RootModuleCache
{
    // Caching allows the module tree to outlive the item component which is replaced each time the ItemStack syncs.
    // This means that non-persistent values such as cooldowns do not have to be saved to NBT.
    private final Map<UUID, RootModuleHolder> serverMap = CacheBuilder.newBuilder().softValues().build().asMap();
    private final Map<UUID, RootModuleHolder> clientMap = CacheBuilder.newBuilder().softValues().build().asMap();

    public RootModuleHolder getOrCreate(UUID uuid, Meatgun meatgun, boolean isServer)
    {
        return isServer ?
                serverMap.computeIfAbsent(uuid, u -> new RootModuleHolder(meatgun))
                : clientMap.computeIfAbsent(uuid, u -> new RootModuleHolder(meatgun));
    }

    public RootModuleCache()
    {
        ServerLifecycleEvents.SERVER_STOPPED.register(server ->
                serverMap.clear());
    }

    public static void init()
    {
    }

//    @OnlyIn(Dist.CLIENT)
//    public static class Client
//    {
//        public static void init()
//        {
//            ClientPlayConnectionEvents.DISCONNECT.register((clientPlayNetworkHandler, minecraftClient) ->
//                    .clear());
//        }
//    }
}
