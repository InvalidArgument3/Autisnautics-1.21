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

package com.neep.neepmeat.transport.blood_network;

import com.google.common.collect.Queues;
import com.neep.neepmeat.transport.TransportComponents;
import com.neep.neepmeat.transport.api.BlockEntityUnloadListener;
import com.neep.neepmeat.transport.api.pipe.VascularConduitEntityProvider;
import com.neep.neepmeat.transport.event.WorldChunkEvents;
import com.neep.neepmeat.transport.interfaces.IServerWorld;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class BloodNetworkManager
{
    private final Object2ObjectMap<UUID, BloodNetwork> tickingNetworks = new Object2ObjectOpenHashMap<>();

    private final ServerLevel world;

    public BloodNetworkManager(ServerLevel world)
    {
        this.world = world;
    }

    public BloodNetwork create(BlockPos pos)
    {
        var network = new ConduitBloodNetwork(UUID.randomUUID(), world);
        tickingNetworks.put(network.getUUID(), network);

        return network;
    }

    private void tick()
    {
        while (!stagedEvents.isEmpty())
        {
            var event = stagedEvents.poll();
            event.run();
        }

        var it = tickingNetworks.entrySet().iterator();
        while (it.hasNext())
        {
            var network = it.next().getValue();
//            var network = entrynetwork();

            if (network.isRemoved())
            {
                it.remove();
            }
            else
            {
                network.tick();
            }
        }
    }

    public static BloodNetworkManager get(Level world)
    {
        if (world instanceof ServerLevel serverWorld)
        {
            return ((IServerWorld) serverWorld).getBloodNetworkManager();
        }
        throw new IllegalArgumentException("Blood network manager must only be used on the logical server.");
    }

    private final Queue<Runnable> stagedEvents = Queues.newArrayDeque();

    public void stageEvent(Runnable runnable)
    {
        stagedEvents.add(runnable);
    }

    public static void init()
    {
        WorldChunkEvents.BE_SET_WORLD.register((chunk, be) ->
        {
            if (be instanceof VascularConduitEntityProvider conduit)
            {
                BloodNetworkChunkComponent component = chunk.getComponent(TransportComponents.BLOOD_NETWORK);
                component.register(conduit.get());
            }
        });

        WorldChunkEvents.BE_MANUAL_REMOVE.register(((chunk, be) ->
        {
            if (be instanceof VascularConduitEntityProvider conduit)
            {
                BloodNetworkChunkComponent component = chunk.getComponent(TransportComponents.BLOOD_NETWORK);
                component.unregister(conduit.get());
            }
        }));

        WorldChunkEvents.UNLOAD_ENTITIES.register(chunk ->
        {
            chunk.getBlockEntities().values().forEach(be ->
            {
                if (be instanceof BlockEntityUnloadListener listener)
                    listener.onUnload(chunk);
            });
        });

        ServerTickEvents.END_WORLD_TICK.register(world ->
        {
            get(world).tick();
        });

        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) ->
        {
            var manager = get(world);
            var component = TransportComponents.BLOOD_NETWORK.get(chunk);
            manager.stageEvent(() ->
            {
                var map = component.getPipes().asMap();
                map.forEach((uuid, pipes) ->
                {
                    manager.tickingNetworks.computeIfAbsent(uuid, u -> new ConduitBloodNetwork(uuid, world))
                            .insert(pipes);
                });
                component.loaded = true;
            });
        });
    }

    record NetworkEntry(BloodNetwork network, Set<UUID> container)
    {
        public UUID uuid()
        {
            return network.getUUID();
        }
    }
}
