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

package com.neep.neepbus.util;

import com.neep.neepbus.block.NeepBusProvider;
import com.neep.neepbus.util.message.DataVariant;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.neepmeat.util.BFSGroupFinder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * For use when there are multiple known output ports that will be used regularly.
 */
public class MultiCachingSender
{
    private final Supplier<Level> world;
    private final BlockPos pos;
    private final Predicate<String> addressPredicate;

    @Nullable private Map<String, Set<WritePort>> portCache;

    public MultiCachingSender(Supplier<Level> world, BlockPos pos, Predicate<String> addressPredicate)
    {
        this.world = world;
        this.pos = pos;
        this.addressPredicate = addressPredicate;
    }

    public void send(String address, DataVariant data)
    {
        if (portCache == null)
        {
            Finder<WritePort> finder = new Finder<>(false);
            finder.queueBlock(pos);
            finder.loop(200);

            portCache = finder.result;
//            portCache.forEach((s, neepBusPorts) -> neepBusPorts.forEach(p -> p.addInvalidateListener(this::invalidate)));
        }

        // If an address is not in the map, a corresponding receiver was not found.
        @Nullable Set<WritePort> ports = portCache.get(address);
        if (ports != null)
        {
            for (var port : portCache.get(address))
            {
                port.write(data);
            }
        }
    }
    
    // TODO: read

    public void invalidate()
    {
        portCache = null;
    }

    private class Finder<T extends NeepBusPort> extends BFSGroupFinder<T>
    {
        private final boolean read;
        private final Map<String, Set<T>> result = new HashMap<>();
        private boolean isOrigin;

        public Finder(boolean read)
        {
            this.read = read;
        }

        protected void addResult(String address, T port)
        {
            result.computeIfAbsent(address, s -> new HashSet<>()).add(port);
        }

        @Override
        protected State processPos(BlockPos pos)
        {
            Level world = MultiCachingSender.this.world.get();
            BlockState currentState = world.getBlockState(pos);
            BlockPos.MutableBlockPos mutable = pos.mutable();

            for (Direction direction : Direction.values())
            {
                mutable.setWithOffset(pos, direction);
                if (visited.contains(mutable.asLong()))
                    continue;

                if (currentState.getBlock() instanceof DataCable dataCable)
                {
                    // Special connection behaviour when this block is the origin of the read/write
                    if (dataCable.isConnected(world, pos, currentState, direction, isOrigin))
                    {
                        BlockState offsetState = world.getBlockState(mutable);

                        if (offsetState.isAir())
                            continue;

                        boolean checkAdjacent = dataCable.checkAdjacentConnection();

                        if (offsetState.getBlock() instanceof DataCable offset
                                && (!checkAdjacent || offset.isConnected(world, mutable, offsetState, direction.getOpposite(), false)))
                        {
                            queueBlock(mutable.immutable());
                        }

                        if (offsetState.getBlock() instanceof NeepBusProvider provider)
                        {
                            // JAAAAAAAAAAAAANK
                            if (read)
                            {
                                for (var entry : provider.getPorts(world, mutable, offsetState, null).readMap().entrySet())
                                {
                                    if (addressPredicate.test(entry.getKey()))
                                        addResult(entry.getKey(), (T) entry.getValue());
                                }
                            }
                            else
                            {
                                for (var entry : provider.getPorts(world, mutable, offsetState, null).writeMap().entrySet())
                                {
                                    if (addressPredicate.test(entry.getKey()))
                                        addResult(entry.getKey(), (T) entry.getValue());
                                }
                            }
                        }
                    }
                }
            }

            isOrigin = false;
            return State.CONTINUE;
        }
    }
}
