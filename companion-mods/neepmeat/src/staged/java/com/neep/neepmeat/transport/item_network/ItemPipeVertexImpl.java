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

package com.neep.neepmeat.transport.item_network;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.meatlib.util.graph.ConnectablePipe;
import com.neep.neepmeat.util.PipeGraphUtil;
import com.neep.meatlib.util.graph.PipeVertex;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.storage.WorldSupplier;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import it.unimi.dsi.fastutil.ints.IntArrays;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.ResourceAmount;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ItemPipeVertexImpl extends ItemPipeGraphHelperImpl implements ItemPipeVertex, NbtSerialisable
{
    public static final int[] NORMAL_INDICES = {0, 1, 2, 3, 4, 5};

    private final ItemPipeVertex[] adjacentVertices = new ItemPipeVertex[6];
    private final int[] sortedIndices = Arrays.copyOf(NORMAL_INDICES, NORMAL_INDICES.length);
    protected final BlockEntity parent;

    public boolean needsMigration;

    @Nullable
    private Long[] queuedPositions = null;

    public ItemPipeVertexImpl(BlockEntity parent)
    {
        super(WorldSupplier.of(parent), parent::getBlockState, (ConnectablePipe) parent.getBlockState().getBlock(), parent.getBlockPos());
        this.parent = parent;
    }

    @Override
    public ItemPipeVertex[] getAdjVertices()
    {
        if (needsMigration)
        {
            needsMigration = false;

            NeepMeat.LOGGER.info("Migrating ItemPipeVertex");
            if (!canSimplify())
            {
                updateConnectionChange(getCachedState(), getCachedState());
            }
        }

        if (queuedPositions != null)
        {
            for (int dir = 0; dir < 6; dir++)
            {
                if (queuedPositions[dir] != null)
                {
                    adjacentVertices[dir] = ItemPipeVertex.find((ServerLevel) parent.getLevel(), BlockPos.of(queuedPositions[dir]));
                }
            }
            queuedPositions = null;

            sort();
        }
        return adjacentVertices;
    }

    @Override
    public int[] getSortedIndices()
    {
        return sortedIndices;
    }

    @Override
    public boolean canSimplify()
    {
        BlockState state = getCachedState();
        return numEdges() <= 2
                && pipe.countConnections(state) <= 2
                && !connectsToEndpoint();
    }

    private boolean connectsToEndpoint()
    {
        for (var face : pipe.getConnections(getCachedState()).directions())
        {
            var otherPipe = ItemPipe.findItemPipe(parent.getLevel().getBlockState(getPos().relative(face)));

            if (otherPipe != null && otherPipe.endPoint())
            {
                return true;
            }

            if (otherPipe == null)
            {
                // Should cover cases where this is connected to a storage or air
                return true;
//                if (ipbe.getStorage(face) != null)
//                    return true;
            }
        }

        return false;
    }

    @Override
    public void setAdjVertex(int dir, PipeVertex vertex)
    {
        setAdjVertex(dir, (ItemPipeVertex) vertex);
    }

    private void sort()
    {
        getAdjVertices();
        // Sort in reverse order so that higher priority edges come first
        IntArrays.quickSort(sortedIndices, (i1, i2) ->
                Boolean.compare(
                        adjacentVertices[i2] != null && adjacentVertices[i2].prioritise(),
                        adjacentVertices[i1] != null && adjacentVertices[i1].prioritise()
                ));
    }

    @Override
    public void setAdjVertex(int dir, ItemPipeVertex vertex)
    {
        adjacentVertices[dir] = vertex;
        sort();
    }

    @Override
    public void clearEdges()
    {
        for (int dir = 0; dir < 6; dir++)
        {
            var adj = getAdjVertex(dir);
            if (adj != null)
            {
                for (int outDir = 0; outDir < 6; outDir++)
                {
                    if (adj.getAdjVertex(outDir) == this)
                    {
                        adj.setAdjVertex(outDir, null);
                        PipeGraphUtil.jankParticles((ServerLevel) parent.getLevel(), adj, false);
                    }
                }

                setAdjVertex(dir, null);
            }
        }
    }

    @Override
    public @Nullable ItemPipe getPipe()
    {
        return (ItemPipe) pipe;
    }

    @Override
    public boolean canItemEnter(ItemVariant variant, long amount, Direction inFace, boolean routed)
    {
        return getPipe().canItemEnter(variant, amount, world.get(), getPos(), getCachedState(), inFace, routed);
    }

    @Override
    public boolean canItemLeave(ResourceAmount<ItemVariant> resource, Direction face, boolean routed)
    {
        return getPipe().canItemLeave(resource, world.get(), getPos(), getCachedState(), face, routed);
    }

    @Override
    public boolean isConnected(Direction face)
    {
        return pipe.isConnected(world.get(), getPos(), getCachedState(), face);
    }

    @Override
    public Direction getDirection(ItemPipeVertex vertex)
    {
        getAdjVertices();
        for (int i = 0; i < 6; i++)
        {
            if (adjacentVertices[i] == vertex)
                return Direction.values()[i];
        }

        return Direction.NORTH;
    }

    @Override
    public @Nullable Storage<ItemVariant> getStorage(Direction direction)
    {
        if (parent instanceof ItemPipeBlockEntity ipbe)
        {
            return ipbe.getStorage(direction);
        }
        return null;
    }

    @Override
    public boolean prioritise()
    {
        return ((ItemPipe) pipe).prioritise();
    }

    @Override
    public int getCurrentOutput(Direction inFace)
    {
        if (parent instanceof ItemPipeBlockEntity be)
        {
            return be.getCurrentOutput(connections.with(inFace, false));
        }

        return -1;
    }

    @Override
    public void nextOutput(Direction inFace)
    {
        if (parent instanceof ItemPipeBlockEntity be)
        {
            be.nextOutput(connections.with(inFace, false));
        }
    }

    protected int numEdges()
    {
        if (needsMigration)
            return 0;

        int edges = 0;
        for (var vertex : getAdjVertices())
        {
            if (vertex != null)
                ++edges;
        }
        return edges;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        ListTag adjacent = new ListTag();
        for (int dir = 0; dir < 6; dir++)
        {
            CompoundTag adjNbt = new CompoundTag();
            adjacent.add(adjNbt);

            // During world loading, block entities can be created, serialised then created again from the new NBT.
            // Also relevant in toInitialChunkDataNbt
            if (queuedPositions != null)
            {
                if (queuedPositions[dir] != null)
                    adjNbt.putLong("pos", queuedPositions[dir]);

                // getAdjVertex can cause a world interaction that could lock the server thread
                continue;
            }

            var adj = getAdjVertex(dir);
            if (adj != null)
            {
                adjNbt.putLong("pos", adj.getPos().asLong());
            }
        }

        nbt.put("adjacent", adjacent);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        ListTag adjacent = nbt.getList("adjacent", Tag.TAG_COMPOUND);

        queuedPositions = new Long[6];
        for (int dir = 0; dir < 6; ++dir)
        {
            CompoundTag adjNbt = adjacent.getCompound(dir);

            if (adjNbt.contains("pos"))
            {
                queuedPositions[dir] = adjNbt.getLong("pos");
            }
        }
    }
}
