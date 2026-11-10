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

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
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
import java.util.Map;

// Okay, I know that casting the results of every function to T is cursed and unsafe, but I... erm...
public class BigBlockPattern
{
    protected Map<Vec3i, BlockStateProvider> stateProviderMap = Maps.newHashMap();
    protected Multimap<Vec3i, BlockApiLookup<?, ?>> apiMap = Multimaps.newSetMultimap(Maps.newHashMap(), Sets::newHashSet);
    
    public <T extends BigBlockPattern> T oddCylinder(int radius, int startHeight, int endHeight, BlockStateProvider state)
    {
        return oddCylinder(Vec3i.ZERO, radius, startHeight, endHeight, state);
    }

    public <T extends BigBlockPattern> T oddCylinder(Vec3i origin, int radius, int startHeight, int endHeight, BlockStateProvider state)
    {
        for (int i = -radius; i <= radius; ++i)
        {
            for (int j = -radius; j <= radius; ++j)
            {
                for (int k = startHeight; k <= endHeight; ++k)
                {
                    set(origin.getX() + i, origin.getY() + k, origin.getZ() + j, state);
                }
            }
        }
        return (T) this;
    }

    public <T extends BigBlockPattern> T range(int startX, int startY, int startZ, int endX, int endY, int endZ, BlockStateProvider state)
    {
        BlockPos.betweenClosed(new BlockPos(startX, startY, startZ), new BlockPos(endX, endY, endZ)).forEach(mutable ->
        {
            set(mutable.immutable(), state);
        });
        return (T) this;
    }

    public static BigBlockPattern makeOddCylinder(int radius, int startHeight, int endHeight, BlockState state)
    {
        return makeOddCylinder(Vec3i.ZERO, radius, startHeight, endHeight, state);
    }

    public static BigBlockPattern makeOddCylinder(Vec3i origin, int radius, int startHeight, int endHeight, BlockState state)
    {
        return new BigBlockPattern().oddCylinder(origin, radius, startHeight, endHeight, () -> state);
    }

    public static BigBlockPattern makeRange(int startX, int startY, int startZ, int endX, int endY, int endZ, BlockState state)
    {
        return new BigBlockPattern().range(startX, startY, startZ, endX, endY, endZ, () -> state);
    }

    public <P extends BigBlockPattern> P  set(int x, int y, int z, BlockState state)
    {
        return set(new Vec3i(x, y, z), () -> state);
    }

    public <P extends BigBlockPattern> P set(int x, int y, int z, BlockStateProvider state)
    {
        return set(new Vec3i(x, y, z), state);
    }

    public <P extends BigBlockPattern> P  set(Vec3i pos, BlockStateProvider state)
    {
        stateProviderMap.put(pos, state);
        return (P) this;
    }

    /**
     * The block entity at the specified location will be flagged with the given API lookup's ID.
     * The structure block entity's API provider still needs to be registered elsewhere.
     */
    public <P extends BigBlockPattern, T, C> P enableApi(int x, int y, int z, BlockApiLookup<T, C> lookup)
    {
        apiMap.put(new Vec3i(x, y, z), lookup);
        return (P) this;
    }

    public <P extends BigBlockPattern> P remove(Vec3i pos)
    {
        stateProviderMap.remove(pos);
        return (P) this;
    }

    public Collection<Vec3i> iterable()
    {
        return stateProviderMap.keySet();
    }

    public Int2ObjectMap<Vec3i> encode()
    {
        Int2ObjectMap<Vec3i> map = new Int2ObjectArrayMap<>();

        int i = 1;
        for (Vec3i offset : iterable())
        {
            if (!offset.equals(Vec3i.ZERO))
            {
                map.put(i, offset);
                i++;
            }
        }

        map.put(0, Vec3i.ZERO);

        return map;
    }

    public Iterable<Pair<Vec3i, BlockState>> entries()
    {
        // I don't think this is reusable. Oh, well.
        return stateProviderMap.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue().get()))::iterator;
    }

    public void placeBlocks(Level world, BlockPos origin, BlockPos controller)
    {
        BlockPos.MutableBlockPos mutable = origin.mutable();
        stateProviderMap.forEach((offset, provider) ->
        {
            mutable.setWithOffset(origin, offset);

            // Do not replace the controller
            if (mutable.equals(controller))
                return;

            world.setBlock(mutable, provider.get(), Block.UPDATE_CLIENTS);
            postProcessBlock(world, offset, mutable, controller, provider.get());
        });
    }

    public Collection<BlockApiLookup<?, ?>> getApi(Vec3i offset)
    {
        return apiMap.get(offset);
    }

    protected void postProcessBlock(Level world, Vec3i offset, BlockPos pos, BlockPos controller, BlockState state)
    {
        if (world.getBlockEntity(pos) instanceof BigBlockStructureEntity be)
        {
            be.setController(controller);
            apiMap.get(offset).forEach(be::enableApi);
            world.blockUpdated(pos, state.getBlock());
        }
    }

    public BigBlockPattern rotateY(float degrees)
    {
        BigBlockPattern newVolume = new BigBlockPattern();

        double s = Math.round(Math.sin(Math.toRadians(degrees)));
        double c = Math.round(Math.cos(Math.toRadians(degrees)));

        stateProviderMap.forEach((offset, state) ->
        {
            int newX = (int) Math.round(offset.getX() * c - offset.getZ() * s);
            int newZ = (int) Math.round(offset.getX() * s + offset.getZ() * c);
            newVolume.set( newX, offset.getY(), newZ, state);
        });

        apiMap.forEach((offset, api) ->
        {
            int newX = (int) Math.round(offset.getX() * c - offset.getZ() * s);
            int newZ = (int) Math.round(offset.getX() * s + offset.getZ() * c);
            newVolume.enableApi(newX, offset.getY(), newZ, api);
        });

        return newVolume;
    }

    public BigBlockPattern mirrorY()
    {
        BigBlockPattern newVolume = new BigBlockPattern();

        stateProviderMap.forEach((offset, state) ->
        {
            newVolume.set(offset.getX(), -offset.getY(), offset.getZ(), state);
        });

        apiMap.forEach((offset, api) ->
        {
            newVolume.enableApi(offset.getX(), -offset.getY(), offset.getZ(), api);
        });

        return newVolume;
    }

    public VoxelShape toVoxelShape()
    {
        return unSimplifiedShape().optimize();
    }

    public VoxelShape unSimplifiedShape()
    {
        VoxelShape shape = Shapes.empty();
        for (Vec3i vec : iterable())
        {
            shape = Shapes.joinUnoptimized(shape, Shapes.box(vec.getX(), vec.getY(), vec.getZ(), vec.getX() + 1, vec.getY() + 1, vec.getZ() + 1), BooleanOp.OR);
//            shape = VoxelShapes.union(shape, VoxelShapes.cuboid(vec.getX(), vec.getY(), vec.getZ(), vec.getX() + 1, vec.getY() + 1, vec.getZ() + 1));
        }
        return shape;
    }

    public AABB toBox(BlockPos origin)
    {
        AABB box = new AABB(0, 0, 0, 0, 0, 0);
        for (Vec3i vec : iterable())
        {
            box = box.minmax(new AABB(vec.getX(), vec.getY(), vec.getZ(), vec.getX() + 1, vec.getY() + 1, vec.getZ() + 1));
        }
        return box.move(origin);
    }

    @FunctionalInterface
    public interface BlockStateProvider
    {
        BlockState get();
    }
}
