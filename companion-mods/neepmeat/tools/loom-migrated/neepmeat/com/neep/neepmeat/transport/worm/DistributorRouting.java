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
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.block.entity.DistributorPoint;
import com.neep.neepmeat.machine.live_machine.block.entity.DistributorPointBlockEntity;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DistributorRouting implements DistributorRoutingComponent, ServerTickingComponent
{
    private final Level world;

    private final Map<BlockPos, PointHandle> handles = new HashMap<>();

    private final List<RouteInProgress> inProgress = new ObjectArrayList<>();
    private final Set<BlockPos> blocked = new HashSet<>();

    public DistributorRouting(Level world)
    {
        this.world = world;

        if (!world.isClientSide())
        {
            var global = GlobalDistributorRouting.getInstance();
            if (global != null)
                global.registerComponent(this);
        }
    }

    public void registerPoint(DistributorPoint point)
    {
        handles.put(point.getPointPos(), new PointHandle(point.getPointPos(), point.getAddress(), point.getMode(), point.isPowered()));
    }

    @Override
    public void unregisterPoint(BlockPos pos)
    {
        handles.remove(pos);
        blocked.remove(pos);
        inProgress.removeIf(p -> p.destination.equals(pos));
    }

    @Override
    public void send(DistributorParcel parcel, DistributorDestination destination, SkywormAnimationTimes times)
    {
        // TODO: worm duration
        inProgress.add(new RouteInProgress(parcel, destination.pos(), world.getGameTime(), times));
        blocked.add(destination.pos());
    }

    @Override
    public RouteLock acquireRoute(BlockPos pos)
    {
        blocked.add(pos);
        return new RouteLock(world, pos);
    }

    @Override
    public void releaseRoute(BlockPos pos)
    {
        blocked.remove(pos);
        inProgress.removeIf(p -> p.destination.equals(pos));
    }

    @Override
    public void collectMatching(List<DistributorDestination> result, String address)
    {
        for (var handle : handles.values())
        {
            if (handle.mode().canReceive()
                    && handle.powered()
                    && handle.matches(address)
                    && !blocked.contains(handle.pos())
            )
                result.add(new DistributorDestination(world, handle.pos()));
        }
    }

    @Override
    public Level getWorld()
    {
        return world;
    }

    @Override
    public void readFromNbt(CompoundTag tag)
    {
        ListTag list = tag.getList("handles", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++)
        {
            CompoundTag compound = list.getCompound(i);
            BlockPos pos = BlockPos.of(compound.getLong("pos"));
            handles.put(pos, PointHandle.fromNbt(compound.getCompound("handle")));
        }

        inProgress.clear();
        blocked.clear();
        inProgress.addAll(DistributorParcel.readList(tag.getList("in_progress",Tag.TAG_COMPOUND), RouteInProgress::fromNbt));
        inProgress.forEach(p -> blocked.add(p.destination));

    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag)
    {
        ListTag list = new ListTag();

        for (var entry : handles.entrySet())
        {
            CompoundTag compound = new CompoundTag();
            compound.putLong("pos", entry.getKey().asLong());
            compound.put("handle", entry.getValue().toNbt());
            list.add(compound);
        }

        tag.put("handles", list);

        tag.put("in_progress", DistributorParcel.writeList(inProgress, RouteInProgress::toNbt));
    }

    @Override
    public void serverTick()
    {
        var it = inProgress.iterator();
        while (it.hasNext())
        {
            var entry = it.next();
            if (world.getGameTime() >= entry.startTime + entry.times.total())
            {
                var be = new DistributorDestination(world, entry.destination).getDistributorPoint();
                if (be != null)
                {
                    be.receive(entry.parcel);
                    blocked.remove(entry.destination);
                    it.remove();
                }
            }
            else if (world.getGameTime() == entry.startTime + entry.times.receiverWormAppear())
            {
                // Send the worm
                BlockPos pos = entry.destination;
                int x = pos.getX();
                int y = pos.getY();
                int z = pos.getZ();

                if (world.hasChunkAt(x, z) && world.hasNearbyAlivePlayer(x, y, z, 60))
                {
                    BlockState state = world.getBlockState(pos);
                    if (state.is(LivingMachines.DISTRIBUTOR_POINT))
                        DistributorPointBlockEntity.createWorm(getWorld(), world.getBlockState(pos), pos, entry.times);

                    // Not sure what to do otherwise.
                }
            }
        }
    }


    record RouteInProgress(DistributorParcel parcel, BlockPos destination, long startTime, SkywormAnimationTimes times)
    {
//        public static final Codec<RouteInProgress> CODEC = RecordCodecBuilder.create(instance ->
//                instance.group(
//                        Codec.INT
//                )

        public CompoundTag toNbt()
        {
            CompoundTag nbt = new CompoundTag();
            nbt.put("parcel", parcel.toNbt());
            nbt.putLong("destination", destination.asLong());
            nbt.putLong("start_time", startTime);
            nbt.put("times", SkywormAnimationTimes.CODEC.encode(times, NbtOps.INSTANCE, new CompoundTag()).result().get()); // Hmm
            return nbt;
        }

        public static RouteInProgress fromNbt(CompoundTag nbt)
        {
            return new RouteInProgress(
                    DistributorParcel.fromNbt(nbt.getCompound("parcel")),
                    BlockPos.of(nbt.getLong("destination")),
                    nbt.getLong("start_time"),
                    SkywormAnimationTimes.CODEC.decode(NbtOps.INSTANCE, nbt.getCompound("times")).getOrThrow(true, s -> {}).getFirst()
            );

        }
    }
}
