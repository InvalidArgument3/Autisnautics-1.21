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
import com.google.common.collect.Sets;
import com.neep.neepmeat.transport.api.pipe.VascularConduit;
import com.neep.neepmeat.transport.api.pipe.VascularConduitEntity;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BloodNetGraph
{
    protected final Level world;
    protected final ConduitBloodNetwork.AcceptorManager acceptors;
    protected Long2ObjectOpenHashMap<VascularConduitEntity> conduits = new Long2ObjectOpenHashMap<>();

    public BloodNetGraph(Level world, ConduitBloodNetwork.AcceptorManager acceptors)
    {
        this.world = world;
        this.acceptors = acceptors;
    }

    public void clear()
    {
        conduits.clear();
    }

    public Long2ObjectMap<VascularConduitEntity> getConduits()
    {
        return conduits;
    }


    public void rebuild(BlockPos startPos)
    {
        clear();

        Set<Long> visited = Sets.newHashSet();
        Queue<BlockPos> posQueue = Queues.newArrayDeque();
        BlockPos.MutableBlockPos mutable = startPos.mutable();

        posQueue.add(startPos);
        visited.add(startPos.asLong());

        while (!posQueue.isEmpty())
        {
            var current = posQueue.poll();
            var currentState = world.getBlockState(current);

            var conduit = VascularConduit.find(world, current, currentState);
            if (conduit != null)
            {
                insert(current.asLong(), conduit.getEntity(world, current, currentState));

                for (Direction direction : Direction.values())
                {
                    if (!conduit.isConnectedIn(world, current, currentState, direction)) continue;

                    mutable.setWithOffset(current, direction);

                    if (visited.contains(mutable.asLong())) continue;
                    visited.add(mutable.asLong());

                    BlockState nextState = world.getBlockState(mutable);

                    VascularConduit nextConduit = VascularConduit.find(world, mutable, nextState);
                    if (nextConduit != null && nextConduit.isConnectedIn(world, mutable, nextState, direction.getOpposite()))
                    {
                        posQueue.add(mutable.immutable());
                    }
                }
            }
        }
    }

    public boolean isEmpty()
    {
        return conduits.isEmpty();
    }

    public void insert(long lpos, VascularConduitEntity newPart)
    {
        conduits.put(lpos, newPart);

        BlockPos pos = BlockPos.of(lpos);
        BlockState state = world.getBlockState(pos);
        var conduit = VascularConduit.find(world, pos, state);
        if (conduit == null) return;

        acceptors.discover(world, pos, state, conduit);

    }

    public void remove(long pos)
    {
        conduits.remove(pos);
    }

    public CompoundTag toNbt()
    {
        CompoundTag root = new CompoundTag();

        var list = new ListTag();

        conduits.long2ObjectEntrySet().fastForEach(entry ->
        {
            list.add(LongTag.valueOf(entry.getLongKey()));
        });

        root.put("conduits", list);

        return root;
    }

    public void readNbt(CompoundTag nbt)
    {
        ListTag list = nbt.getList("conduits", Tag.TAG_LONG);

        for (Tag entry : list)
        {
            if (entry instanceof LongTag l)
            {
                long lpos = l.getAsLong();
                BlockPos pos = BlockPos.of(lpos);
                BlockState state = world.getBlockState(pos);

//                VascularConduit conduit = VascularConduit.find(world, pos, world.getBlockState(pos));
//                if (conduit == null) continue;
//
//                VascularConduitEntity entity = conduit.getEntity(world, pos, state);
                var entity = VascularConduitEntity.find(world, pos);
                if (entity == null) continue;

                conduits.put(lpos, entity);
            }
        }
    }
}
