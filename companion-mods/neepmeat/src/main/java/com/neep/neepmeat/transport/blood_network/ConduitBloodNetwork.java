/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Each tick sums source output, divides by the reference power of 1000, gives that
 * per-unit flow to active sinks first, then splits the remainder across passive sinks.
 */

package com.neep.neepmeat.transport.blood_network;

import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import com.neep.neepmeat.transport.block.VascularConduitBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConduitBloodNetwork {
    private final ServerLevel level;
    private final UUID id = UUID.randomUUID();
    private final Map<Long, VascularConduitBlockEntity> conduits = new java.util.HashMap<>();
    private final List<BloodAcceptor> sources = new ArrayList<>();
    private final List<BloodAcceptor> activeSinks = new ArrayList<>();
    private final List<BloodAcceptor> sinks = new ArrayList<>();
    private final IdentityHashMap<BloodAcceptor, Boolean> known = new IdentityHashMap<>();
    private boolean removed;

    public ConduitBloodNetwork(ServerLevel level) {
        this.level = level;
    }

    public UUID id() {
        return id;
    }

    public int conduitCount() {
        return conduits.size();
    }

    public boolean removed() {
        return removed;
    }

    public void add(VascularConduitBlockEntity part) {
        conduits.put(part.getBlockPos().asLong(), part);
        part.setNetwork(this);
        discover(part.getBlockPos());
    }

    public void merge(ConduitBloodNetwork other) {
        if (other == this) {
            return;
        }
        List<VascularConduitBlockEntity> moved = new ArrayList<>(other.conduits.values());
        other.conduits.clear();
        other.sources.clear();
        other.sinks.clear();
        other.activeSinks.clear();
        other.known.clear();
        other.removed = true;
        for (VascularConduitBlockEntity conduit : moved) {
            add(conduit);
        }
    }

    public void tick() {
        long internal = 0;
        for (BloodAcceptor source : sources) {
            internal += source.output();
        }
        float remaining = (float) internal / PowerUtil.BASE_POWER;
        for (BloodAcceptor sink : activeSinks) {
            remaining = Math.max(0, remaining - sink.updateInflux(remaining));
        }
        float each = sinks.isEmpty() ? 0 : remaining / sinks.size();
        for (BloodAcceptor sink : sinks) {
            sink.updateInflux(each);
        }
    }

    private void discover(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            if (!com.neep.neepmeat.transport.block.VascularConduitBlock.connects(level, pos, direction)) {
                continue;
            }
            BloodAcceptor acceptor = BloodAcceptor.find(level, neighbor);
            if (acceptor == null || known.put(acceptor, Boolean.TRUE) != null) {
                continue;
            }
            switch (acceptor.mode()) {
                case SOURCE -> sources.add(acceptor);
                case ACTIVE_SINK -> activeSinks.add(acceptor);
                case SINK -> sinks.add(acceptor);
                case BALANCE -> {
                }
            }
        }
    }
}
