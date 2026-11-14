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

package com.neep.neepmeat.api.big_block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BigBlockPattern {
    protected final Map<Vec3i, BlockStateProvider> stateProviderMap = new HashMap<>();
    private final Map<Vec3i, java.util.Set<String>> apis = new HashMap<>();

    public static BigBlockPattern makeOddCylinder(int radius, int startHeight, int endHeight, BlockState state) {
        return new BigBlockPattern().oddCylinder(radius, startHeight, endHeight, () -> state);
    }

    public static BigBlockPattern makeRange(int startX, int startY, int startZ, int endX, int endY, int endZ, BlockState state) {
        return new BigBlockPattern().range(startX, startY, startZ, endX, endY, endZ, () -> state);
    }

    public BigBlockPattern set(int x, int y, int z, BlockState state) {
        return set(x, y, z, () -> state);
    }

    public BigBlockPattern enableApi(int x, int y, int z, String api) {
        apis.computeIfAbsent(new Vec3i(x, y, z), ignored -> new java.util.HashSet<>()).add(api);
        return this;
    }

    public boolean hasApi(Vec3i offset, String api) {
        java.util.Set<String> found = apis.get(offset);
        return found != null && found.contains(api);
    }

    public java.util.Map<Integer, Vec3i> encode() {
        java.util.Map<Integer, Vec3i> map = new java.util.HashMap<>();
        int index = 1;
        for (Vec3i offset : iterable()) {
            if (!offset.equals(Vec3i.ZERO)) {
                map.put(index++, offset);
            }
        }
        map.put(0, Vec3i.ZERO);
        return map;
    }

    public BigBlockPattern oddCylinder(int radius, int startHeight, int endHeight, BlockStateProvider state) {
        for (int i = -radius; i <= radius; ++i) {
            for (int j = -radius; j <= radius; ++j) {
                for (int k = startHeight; k <= endHeight; ++k) {
                    set(i, k, j, state);
                }
            }
        }
        return this;
    }

    public BigBlockPattern range(int startX, int startY, int startZ, int endX, int endY, int endZ, BlockStateProvider state) {
        BlockPos.betweenClosed(new BlockPos(startX, startY, startZ), new BlockPos(endX, endY, endZ)).forEach(mutable -> set(mutable.immutable(), state));
        return this;
    }

    public BigBlockPattern set(int x, int y, int z, BlockStateProvider state) {
        return set(new Vec3i(x, y, z), state);
    }

    public BigBlockPattern set(Vec3i pos, BlockStateProvider state) {
        stateProviderMap.put(pos, state);
        return this;
    }

    public Collection<Vec3i> iterable() {
        return stateProviderMap.keySet();
    }

    public void placeBlocks(Level world, BlockPos origin, BlockPos controller) {
        BlockPos.MutableBlockPos mutable = origin.mutable();
        stateProviderMap.forEach((offset, provider) -> {
            mutable.setWithOffset(origin, offset);
            if (mutable.equals(controller)) {
                return;
            }
            BlockState state = provider.get();
            world.setBlock(mutable, state, Block.UPDATE_ALL);
            if (world.getBlockEntity(mutable) instanceof BigBlockStructureEntity entity) {
                entity.setController(controller);
            }
        });
    }

    public BigBlockPattern rotateY(int degrees) {
        BigBlockPattern rotated = new BigBlockPattern();
        double radians = Math.toRadians(degrees);
        double sine = Math.round(Math.sin(radians));
        double cosine = Math.round(Math.cos(radians));
        stateProviderMap.forEach((offset, state) -> {
            int x = (int) Math.round(offset.getX() * cosine - offset.getZ() * sine);
            int z = (int) Math.round(offset.getX() * sine + offset.getZ() * cosine);
            rotated.set(x, offset.getY(), z, state);
        });
        apis.forEach((offset, names) -> {
            int x = (int) Math.round(offset.getX() * cosine - offset.getZ() * sine);
            int z = (int) Math.round(offset.getX() * sine + offset.getZ() * cosine);
            for (String name : names) {
                rotated.enableApi(x, offset.getY(), z, name);
            }
        });
        return rotated;
    }

    public VoxelShape toVoxelShape() {
        VoxelShape shape = Shapes.empty();
        for (Vec3i vec : iterable()) {
            shape = Shapes.joinUnoptimized(shape, Shapes.box(vec.getX(), vec.getY(), vec.getZ(), vec.getX() + 1, vec.getY() + 1, vec.getZ() + 1), BooleanOp.OR);
        }
        return shape.optimize();
    }

    public AABB toBox(BlockPos origin) {
        AABB box = new AABB(0, 0, 0, 0, 0, 0);
        for (Vec3i vec : iterable()) {
            box = box.minmax(new AABB(vec.getX(), vec.getY(), vec.getZ(), vec.getX() + 1, vec.getY() + 1, vec.getZ() + 1));
        }
        return box.move(origin);
    }

    @FunctionalInterface
    public interface BlockStateProvider {
        BlockState get();
    }
}
