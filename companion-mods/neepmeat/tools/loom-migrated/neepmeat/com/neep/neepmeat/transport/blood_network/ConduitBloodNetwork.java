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

import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.transport.api.pipe.BalancingBloodAcceptor;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import com.neep.neepmeat.transport.api.pipe.VascularConduit;
import com.neep.neepmeat.transport.api.pipe.VascularConduitEntity;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ConduitBloodNetwork implements BloodNetwork
{
    protected final ServerLevel world;
    protected final UUID uuid;

    protected final BloodNetGraph conduits;
    protected AcceptorManager acceptors = new AcceptorManager(this);

    protected boolean updateSinks = false;

    protected long stored = 0;
    protected long lastInternal = 0;
    protected boolean removed = false;

    public ConduitBloodNetwork(UUID uuid, ServerLevel world)
    {
        this.uuid = uuid;
        this.world = world;
        this.conduits = new BloodNetGraph(world, acceptors);
    }

    protected boolean validate()
    {
        if (conduits.isEmpty())
        {
            conduits.clear();
            acceptors.clear();
            removed = true;
            return false;
        }
        return true;
    }

    @Override
    public void rebuild(BlockPos pos, VascularConduitEntity.UpdateReason reason)
    {
        // Disown all conduits
        conduits.getConduits().values().forEach(c -> c.setNetwork(null));
        acceptors.stream().forEach(this::removeAcceptor);

        acceptors.clear();
        updateSinks = false;

        // Rediscover conduits and acceptors, leaving disconnected branches with null network.
        conduits.rebuild(pos);

        if (!validate())
            return;

        conduits.getConduits().values().forEach(c -> c.setNetwork(this));
    }

    @Override
    public void tick()
    {
        long internal = 0;

        if (acceptors.sort)
            acceptors.sort();

        var it = acceptors.sources().iterator();
        while (it.hasNext())
        {
            var acceptor = it.next();
            internal += acceptor.getOutput();
        }

        // Balancing
        for (var balancer : acceptors.balancers().asList())
        {
            internal += balancer.update(internal);
        }

        int size = acceptors.sinks().asList().size();

        if (internal != lastInternal)
        {
            updateSinks = true;
            lastInternal = internal;
        }

        if (updateSinks)
        {
            float outputPU = ((float) (internal)) / PowerUtil.referencePower();
            for (var consumer : acceptors.activeSinks().asList())
            {
                float inserted = consumer.updateInflux(outputPU);
                outputPU = Math.max(0, outputPU - inserted);
            }

            // Passive sinks will soak up the remainder.
            float perSink = size != 0 ? outputPU / size : 0;
            for (var sink : acceptors.sinks().asList())
            {
                sink.updateInflux(perSink);
            }

            updateSinks = false;
        }

//        var sit = sinkUpdateQueue.iterator();
//        while (sit.hasNext())
//        {
//            var sink = sit.next();
//            sit.remove();
//
//            sink.updateInflux(((float) out) / PowerUtils.referencePower());
//        }
    }

    @Override
    public void updateTransfer(@Nullable BloodAcceptor changed)
    {
        updateSinks = true;
    }

    @Override
    public void add(BlockPos pos, VascularConduitEntity newPart)
    {
        insert(pos.asLong(), newPart);
    }

    @Override
    public void insert(Collection<VascularConduitEntity> pipes)
    {
        // TOOD: more efficient acceptor discovery
        pipes.forEach(p ->
        {
            conduits.insert(p.getBlockPos().asLong(), p);
            p.setNetwork(this);
        });
    }

    public void insert(long pos, VascularConduitEntity newPart)
    {
        conduits.insert(pos, newPart); // Acceptors will be detected automatically
        newPart.setNetwork(this);

        updateSinks = true;
    }

    @Override
    public void remove(BlockPos pos, VascularConduitEntity part)
    {
        acceptors.get(pos).forEach(this::removeAcceptor);
        acceptors.remove(pos);

        conduits.remove(pos.asLong());

        updateSinks = true;

        validate();
    }

    @Override
    public void unload(BlockPos pos, VascularConduitEntity part)
    {
        acceptors.remove(pos);
        conduits.remove(pos.asLong());
    }

    @Override
    public void update(BlockPos pos, VascularConduitEntity part)
    {
        acceptors.get(pos).forEach(this::removeAcceptor);

        acceptors.remove(pos);
        conduits.remove(pos.asLong());
        conduits.insert(pos.asLong(), part);

        updateSinks = true;

        validate();
    }

    private void removeAcceptor(BloodAcceptor acceptor)
    {
        if (acceptor == null)
            return;

        acceptor.updateInflux(0);
        acceptor.setChangeListener(null);
    }

    public void mergeInto(BloodNetwork network)
    {
        if (network instanceof ConduitBloodNetwork other)
        {
            conduits.getConduits().forEach(other::insert);
            acceptors.mergeInto(other.acceptors);

            conduits.clear();
            acceptors.clear();
            removed = true;
        }
        else throw new NotImplementedException();
    }

    public void merge(List<BloodNetwork> adjNetworks)
    {
        for (var network : adjNetworks)
        {
            if (network == this) continue;

            network.mergeInto(this);
        }

        acceptors.sort();
        updateSinks = true;
    }

    public boolean isRemoved()
    {
        return removed;
    }

    @Override
    public UUID getUUID()
    {
        return uuid;
    }

    protected class AcceptorManager
    {
        private final ConduitBloodNetwork network;

        protected PosDirectionMap<BloodAcceptor> acceptors = new PosDirectionMap<>(BloodAcceptor.class);
        protected PosDirectionMap<BloodAcceptor> sources = new PosDirectionMap<>(BloodAcceptor.class);
        protected PosDirectionMap<BloodAcceptor> sinks = new PosDirectionMap<>(BloodAcceptor.class);
        protected PosDirectionMap<BloodAcceptor> activeSinks = new PosDirectionMap<>(BloodAcceptor.class);
        protected PosDirectionMap<BalancingBloodAcceptor> balancers = new SortedPosDirectionMap<>(BalancingBloodAcceptor.class, Comparator.comparingInt(BalancingBloodAcceptor::priority).reversed());
        public boolean sort = false;

        public AcceptorManager(ConduitBloodNetwork network)
        {
            this.network = network;
        }

        public void discover(Level world, BlockPos pos, BlockState state, VascularConduit conduit)
        {
            BlockPos.MutableBlockPos mutable = pos.mutable();
            for (Direction direction : Direction.values())
            {
                if (!conduit.isConnectedIn(world, pos, state, direction)) continue;

                mutable.setWithOffset(pos, direction);

                BloodAcceptor acceptor = BloodAcceptor.SIDED.find(world, mutable, direction.getOpposite());
                if (acceptor != null)
                {
                    add(pos.asLong(), direction.get3DDataValue(), acceptor);
                    updateSinks = true;
                }
            }
        }

        public Iterable<BloodAcceptor> get(BlockPos pos)
        {
            return acceptors.get(pos.asLong());
        }

        public Stream<BloodAcceptor> stream()
        {
            return acceptors.flatStream();
        }

        public void mergeInto(AcceptorManager other)
        {
            acceptors.fastForEach(entry ->
            {
                for (int dir = 0; dir < 6; ++dir)
                {
                    var a = entry.getValue();

                    if (a[dir] != null)
                        a[dir].setChangeListener(other.network);

                    other.acceptors.put(entry.getLongKey(), dir, a[dir]);
                }
            });
            clear();
            other.sort = true;
        }

        public void add(long pos, int dir, BloodAcceptor acceptor)
        {
            acceptors.put(pos, dir, acceptor);
            acceptor.setChangeListener(ConduitBloodNetwork.this);
            sortAcceptor(pos, dir, acceptor);
        }

        public void sort()
        {
            sources.clear();
            sinks.clear();
            activeSinks.clear();
            balancers.clear();

            acceptors.fastForEach(entry ->
            {
                for (int dir = 0; dir < 6; ++dir)
                {
                    var acceptor = entry.getValue()[dir];
                    if (acceptor == null) continue;

                    long pos = entry.getLongKey();
                    sortAcceptor(pos, dir, acceptor);
                }
            });
            sort = false;
        }

        private void sortAcceptor(long pos, int dir, BloodAcceptor acceptor)
        {
            switch (acceptor.getMode())
            {
                case SOURCE -> sources.put(pos, dir, acceptor);
                case SINK -> sinks.put(pos, dir, acceptor);
                case ACTIVE_SINK -> activeSinks.put(pos, dir, acceptor);
                case BALANCE -> balancers.put(pos, dir, (BalancingBloodAcceptor) acceptor);
            }
        }

        public void remove(BlockPos pos)
        {
            long lpos = pos.asLong();

            acceptors.remove(lpos);
            sources.remove(lpos);
            sinks.remove(lpos);
            activeSinks.remove(lpos);
            balancers.remove(lpos);
        }

        public void clear()
        {
            acceptors.clear();
            sources.clear();
            sinks.clear();
            activeSinks.clear();
            balancers.clear();
        }

        public Stream<BloodAcceptor> sources()
        {
            return sources.flatStream();
        }

        public PosDirectionMap<BloodAcceptor> sinks()
        {
            return sinks;
        }

        public PosDirectionMap<BloodAcceptor> activeSinks()
        {
            return activeSinks;
        }

        public PosDirectionMap<BalancingBloodAcceptor> balancers()
        {
            return balancers;
        }
    }
}
