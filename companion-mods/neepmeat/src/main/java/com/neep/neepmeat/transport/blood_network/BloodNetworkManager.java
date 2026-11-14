/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.transport.blood_network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class BloodNetworkManager {
    private static final Map<ServerLevel, BloodNetworkManager> ALL = new WeakHashMap<>();

    private final List<ConduitBloodNetwork> networks = new ArrayList<>();

    private BloodNetworkManager() {}

    public static BloodNetworkManager get(Level level) {
        return ALL.computeIfAbsent((ServerLevel) level, ignored -> new BloodNetworkManager());
    }

    public static void onTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            BloodNetworkManager manager = ALL.get(level);
            if (manager != null) {
                manager.tick();
            }
        }
    }

    public ConduitBloodNetwork create(ServerLevel level) {
        ConduitBloodNetwork network = new ConduitBloodNetwork(level);
        networks.add(network);
        return network;
    }

    public void tick() {
        Iterator<ConduitBloodNetwork> iterator = networks.iterator();
        while (iterator.hasNext()) {
            ConduitBloodNetwork network = iterator.next();
            if (network.removed()) {
                iterator.remove();
            } else {
                network.tick();
            }
        }
    }
}
