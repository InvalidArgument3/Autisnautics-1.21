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

package com.neep.neepmeat.transport.worm;

import com.neep.neepmeat.entity.skyworm.SkywormAnimationTimes;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.machine.live_machine.block.entity.DistributorPoint;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/// Manages routing between worlds
public class GlobalDistributorRouting
{
    @Nullable
    private static GlobalDistributorRouting INSTANCE = null;
    private final MinecraftServer server;

    private final Map<ResourceKey<Level>, DistributorRoutingComponent> components = new HashMap<>();

    public GlobalDistributorRouting(MinecraftServer server)
    {
        this.server = server;
    }

    public static GlobalDistributorRouting getInstance()
    {
        return INSTANCE;
    }

    public void registerComponent(DistributorRoutingComponent component)
    {
        components.put(component.getWorld().dimension(), component);
    }

    /// Helper method for updating a reference in the world's component
    public static void updatePoint(DistributorPoint point)
    {
        NMComponents.DISTRIBUTOR.maybeGet(point.getPointWorld()).ifPresent(d -> d.registerPoint(point));
    }

    public static void unregisterPoint(Level world, BlockPos pos)
    {
        NMComponents.DISTRIBUTOR.maybeGet(world).ifPresent(d -> d.unregisterPoint(pos));
    }

    private void onStart()
    {
        for (var world : server.getAllLevels())
        {
            // Ensure components are initialised
            var component = NMComponents.DISTRIBUTOR.get(world);
            registerComponent(component);
        }
    }

    public List<DistributorDestination> getMatchingDest(String address)
    {
        List<DistributorDestination> result = new ObjectArrayList<>();
        for (var val : components.values())
        {
            val.collectMatching(result, address);
        }

        return result;
    }

    @Nullable
    public DistributorDestination findAndRoute(DistributorDestination exclude, String address)
    {
        List<DistributorDestination> result = getMatchingDest(address);
        Collections.shuffle(result);
        for (var entry : result)
        {
            if (!entry.equals(exclude))
            {
                return entry;
            }
        }

        return null;
    }

    public void send(DistributorParcel parcel, DistributorDestination destination, SkywormAnimationTimes times)
    {
        var component = NMComponents.DISTRIBUTOR.get(destination.world());
        component.send(parcel, destination, times);
    }

    public static void init()
    {
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
        {
            INSTANCE = new GlobalDistributorRouting(server);

            INSTANCE.onStart();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
        {
            INSTANCE = null;
        });
    }

    public RouteLock acquireRoute(DistributorDestination dest)
    {
        return NMComponents.DISTRIBUTOR.get(dest.world()).acquireRoute(dest.pos());
    }

    public void releaseRoute(DistributorDestination dest)
    {
        NMComponents.DISTRIBUTOR.get(dest.world()).releaseRoute(dest.pos());
    }
}
