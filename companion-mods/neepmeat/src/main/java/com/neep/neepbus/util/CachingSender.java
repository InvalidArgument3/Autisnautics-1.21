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
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CachingSender
{
    private final Supplier<Level> world;
    private final BlockPos pos;

    // Values can be null, empty, or full. Empty indicates that the search has already been run but the
    // target address is not present in the network.
    private final Map<String, Set<WritePort>> writePortCache = new HashMap<>();
    private final Map<String, Set<ReadPort>> readPortCache = new HashMap<>();

    public CachingSender(Supplier<Level> world, BlockPos pos)
    {
        this.world = world;
        this.pos = pos;
    }

    public void send(String address, DataVariant data)
    {
        @Nullable Set<WritePort> cached = writePortCache.get(address);
        if (cached == null)
        {
            Finder<WritePort> finder = new Finder<>(address, false);
            finder.queueBlock(pos);
            finder.loop(200);

            cached = new HashSet<>(finder.getResult().values());
//            cached.forEach(port -> port.addInvalidateListener(this::invalidate));
            writePortCache.put(address, cached);
        }

        for (var port : cached)
        {
            port.write(data);
        }
    }

    public DataVariant read(String address)
    {
        @Nullable Set<ReadPort> cached = readPortCache.get(address);
        if (cached == null)
        {
            Finder<ReadPort> finder = new Finder<>(address, true);
            finder.queueBlock(pos);
            finder.loop(200);

            cached = new HashSet<>(finder.getResult().values());
//            cached.forEach(port -> port.addInvalidateListener(this::invalidate));
            readPortCache.put(address, cached);
        }

        if (!cached.isEmpty())
        {
            return cached.iterator().next().read();
        }

        return DataVariant.NULL;
    }

    public void invalidate()
    {
        writePortCache.clear();
        readPortCache.clear();
    }

    private class Finder<T extends NeepBusPort> extends BFSGroupFinder<T>
    {
        private final String address;
        private final boolean read;
        private boolean isOrigin = true;

        public Finder(String address, boolean read)
        {
            this.address = address;
            this.read = read;
        }

        @Override
        public BFSGroupFinder<T> queueBlock(BlockPos pos)
        {
            posQueue.add(pos);
            visited.add(pos.asLong());
            return this;
        }

        @Override
        protected State processPos(BlockPos pos)
        {
            Level world = CachingSender.this.world.get();
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
                    if (dataCable.isConnected(world, mutable, currentState, direction, isOrigin))
                    {

                        BlockState offsetState = world.getBlockState(mutable);

                        if (offsetState.isAir())
                            continue;

                        boolean checkAdjacent = dataCable.checkAdjacentConnection();

                        if (offsetState.getBlock() instanceof DataCable offset
                                && (!checkAdjacent || offset.isConnected(world, mutable, offsetState, direction.getOpposite(), false)))
                        {
                            // Only mark visited if the cable is connected.
                            // Otherwise, adjacent but disconnected cables that are part of the same network will not work.
                            visited.add(mutable.asLong());
                            queueBlock(mutable.immutable());
                        }

                        if (offsetState.getBlock() instanceof NeepBusProvider provider)
                        {
                            NeepBusPort port = read ?
                                    provider.getPorts(world, mutable, offsetState, direction.getOpposite()).readMap().get(address)
                                    : provider.getPorts(world, mutable, offsetState, direction.getOpposite()).writeMap().get(address);
                            if (port != null)
                            {
                                addResult(mutable, (T) port);
                                visited.add(mutable.asLong());
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
