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

import com.google.common.collect.*;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.api.pipe.VascularConduitEntity;
import com.neep.neepmeat.transport.api.pipe.VascularConduitEntityProvider;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;

public class BloodNetworkChunkComponent implements Component, ServerTickingComponent
{
    private final ChunkAccess chunk;

    private final Set<VascularConduitEntity> pipes = Collections.newSetFromMap(new WeakHashMap<>());
    private final Multimap<UUID, VascularConduitEntity> loadedPipes = Multimaps.newSetMultimap(Maps.newHashMap(), Sets::newHashSet);
    private final Queue<Pair<UUID, BlockPos>> pipesToLoad = Queues.newArrayDeque();

    // Networks are loaded in the tick loop
    public boolean loaded = false;

    public BloodNetworkChunkComponent(ChunkAccess chunk)
    {
        this.chunk = chunk;
    }

    @Override
    public void serverTick()
    {
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag nbt)
    {
        ListTag list = nbt.getList("pipes", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i)
        {
            CompoundTag compound = list.getCompound(i);
            UUID uuid = compound.getUUID("uuid");
            BlockPos pos = BlockPos.of(compound.getLong("pos"));

            pipesToLoad.add(Pair.of(uuid, pos));
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag nbt)
    {
        // I don't think ProtoChunks can ever have pipes in them.
        if (chunk instanceof ProtoChunk)
            return;

        // Pipes are added to networks on the first tick, so it is possible that this method will be called before that happens.
        // In this situation, we need to save pipesToLoad instead of loadedPipes.
        ListTag list = new ListTag();
        if (loaded)
        {
            saveLoadedPipes(list);
        }
        else
        {
            savePipesToLoad(list);
        }
        nbt.put("pipes", list);
    }

    void saveLoadedPipes(ListTag list)
    {
        for (var pipe : pipes)
        {
            var network = pipe.getNetwork();

            // There is no way of distinguishing between unloading and removal of BlockEntities.
            // Dead BEs will stay in the set but will not be saved.
//            if (chunk.getBlockEntityPositions().contains(pipe.getBlockPos()) && network != null)

            if (network == null)
            {
                NeepMeat.LOGGER.warn("Vascular Conduit at " + pipe.getBlockPos() + "has a null network. This indicates that saving or loading has failed.");
                continue;
            }

            UUID uuid = network.getUUID();;
            CompoundTag entry = new CompoundTag();
            entry.putLong("pos", pipe.getBlockPos().asLong());
            entry.putUUID("uuid", uuid);
            list.add(entry);
        }
    }

    void savePipesToLoad(ListTag list)
    {
        for (var pair : pipesToLoad)
        {
            CompoundTag compound = new CompoundTag();
            compound.putUUID("uuid", pair.first());
            compound.putLong("pos", pair.second().asLong());
            list.add(compound);
        }

    }

    @Override
    public boolean equals(Object o)
    {
        return false;
    }

    public Multimap<UUID, VascularConduitEntity> getPipes()
    {
        if (!pipesToLoad.isEmpty())
        {
            pipes.clear();

            while (!pipesToLoad.isEmpty())
            {
                var pair = pipesToLoad.poll();
                var entity = ((VascularConduitEntityProvider) chunk.getBlockEntity(pair.second())).get();

                // Invalid NBT or saving weirdness can cause this.
                if (entity == null)
                    continue;

                loadedPipes.put(pair.first(), entity);
                pipes.add(entity);
            }

            chunk.setUnsaved(true);
        }
        return loadedPipes;
    }

    public void register(VascularConduitEntity vascularConduitEntity)
    {
        pipes.add(vascularConduitEntity);
        chunk.setUnsaved(true);
    }

    public void unregister(VascularConduitEntity vascularConduitEntity)
    {
        pipes.remove(vascularConduitEntity);
        chunk.setUnsaved(true);
    }
}
