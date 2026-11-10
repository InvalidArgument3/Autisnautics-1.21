/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.big_block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BigBlockPattern {
    private final Map<Vec3i, BlockState> states = new HashMap<>();
    private final Map<Vec3i, Set<String>> apis = new HashMap<>();

    public static BigBlockPattern makeOddCylinder(int radius, int startHeight, int endHeight, BlockState state) {
        BigBlockPattern pattern = new BigBlockPattern();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = startHeight; y <= endHeight; y++) {
                    pattern.set(x, y, z, state);
                }
            }
        }
        return pattern;
    }

    public static BigBlockPattern makeRange(int startX, int startY, int startZ, int endX, int endY, int endZ, BlockState state) {
        BigBlockPattern pattern = new BigBlockPattern();
        BlockPos.betweenClosed(new BlockPos(startX, startY, startZ), new BlockPos(endX, endY, endZ))
                .forEach(pos -> pattern.set(pos.getX(), pos.getY(), pos.getZ(), state));
        return pattern;
    }

    public BigBlockPattern set(int x, int y, int z, BlockState state) {
        states.put(new Vec3i(x, y, z), state);
        return this;
    }

    public BigBlockPattern enableApi(int x, int y, int z, String api) {
        apis.computeIfAbsent(new Vec3i(x, y, z), ignored -> new HashSet<>()).add(api);
        return this;
    }

    public boolean hasApi(Vec3i offset, String api) {
        Set<String> found = apis.get(offset);
        return found != null && found.contains(api);
    }

    public Collection<Vec3i> iterable() {
        return states.keySet();
    }

    public Map<Integer, Vec3i> encode() {
        Map<Integer, Vec3i> map = new HashMap<>();
        int index = 1;
        for (Vec3i offset : iterable()) {
            if (!offset.equals(Vec3i.ZERO)) {
                map.put(index++, offset);
            }
        }
        map.put(0, Vec3i.ZERO);
        return map;
    }
}
