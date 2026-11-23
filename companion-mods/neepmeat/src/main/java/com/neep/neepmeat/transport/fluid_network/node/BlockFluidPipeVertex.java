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

package com.neep.neepmeat.transport.fluid_network.node;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.transport.api.FluidTransportConstants;
import com.neep.neepmeat.transport.api.pipe.FluidPipe;
import com.neep.neepmeat.transport.fluid_network.*;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageUtil;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Arrays;
import java.util.Random;

@SuppressWarnings("UnstableApiUsage")
public class BlockFluidPipeVertex extends SimpleFluidPipeVertex implements NbtSerialisable
{
    protected final FluidPipeBlockEntity<?> parent;
    protected final FluidNode[] nodes = new FluidNode[6];
    private final ObjectArrayList<PipeFlowComponent> components = new ObjectArrayList<>(6);
    protected boolean dirty;
    protected Random jrandom = new Random();

    protected Long[] queuedPositions = null;
    protected boolean[] queuedNodes = null;

    // Cache to avoid performing stupid loops every tick. Set to null to invalidate.
    // Invalidate if:
    // - Nodes change
    // - Edges change
    // - BlockState changes
    protected Boolean canSimplify = null;

    public BlockFluidPipeVertex(FluidPipeBlockEntity<?> fluidPipeBlockEntity)
    {
        super(fluidPipeBlockEntity.getBlockPos().asLong());
        this.parent = fluidPipeBlockEntity;
        setHeight(fluidPipeBlockEntity.getBlockPos().getY());
        components.size(6);
    }

    public void invalidateCanSimplify()
    {
        canSimplify = null;
    }

    @Override
    public boolean canSimplify(FluidPipe pipe, BlockState state)
    {
        return super.canSimplify() && numNodes() == 0 && pipe.countConnections(state) <= 2;
    }

    @Override
    public boolean canSimplify()
    {
        if (canSimplify == null)
        {
            canSimplify = canSimplify((FluidPipe) parent.getBlockState().getBlock(), parent.getBlockState());
        }

        return canSimplify;
    }

    @Override
    public void tickDeferredLoad()
    {
        deferredLoad();
    }

    @Override
    public void setAdjVertex(int dir, FluidPipeVertex vertex)
    {
        super.setAdjVertex(dir, vertex);
        parent.setChanged();
        invalidateCanSimplify();
    }

    public int numNodes()
    {
        int number = 0;
        for (var node : nodes)
        {
            if (node != null) ++number;
        }
        return number;
    }

    @Override
    public void updateNodes(ServerLevel world, BlockPos pos, BlockState state)
    {
        Arrays.fill(nodes, null);
        FluidPipe pipe = FluidPipe.findFluidPipe(world, pos, state);
        if (pipe != null)
        {
            for (Direction direction : pipe.getConnections(state).directions())
            {
                FluidNode node = parent.getNode(direction);
                if (node != null)
                {
                    nodes[direction.ordinal()] = node;
                }
            }
        }

        invalidateCanSimplify();
        parent.setChanged();
    }

    protected float nodeHeight(int dir)
    {
        if (!FluidTransportConstants.INSTANCE.nodeGravityEnabled())
            return 0;

//        float height = getHeight();

        return switch (dir)
        {
            case 0 -> - 0.5f;
            case 1 -> + 0.5f;
            default -> 0;
        };
    }

    @Override
    public void reset()
    {
        pumpHeight = 0;
        height = parent.getBlockPos().getY();
        clearEdges();
    }

    @Override
    public void preTick()
    {
//        deferredLoad();
        stepHeight();

        try (Transaction transaction = Transaction.openOuter())
        {
            for (FluidNode node : nodes)
            {
                if (node == null) continue;

                Storage<FluidVariant> storage = node.getStorage((ServerLevel) parent.getLevel());

                float f = getNodeFlow(node.getNodePos(), node);

                // Calculate the amount with respect to this vertex.
                long transferAmount = - (long) Math.ceil(f * getCapacity());
                if (transferAmount > 0)
                {
                    FluidVariant foundVariant = StorageUtil.findExtractableResource(storage, transaction);
                    long extracted;
                    if (foundVariant != null && (variant.isBlank() || foundVariant.equals(variant)))
                    {
                        // Note the negative signs
                        long permittedAmount = canInsert((ServerLevel) parent.getLevel(), node.getNodePos().face().getOpposite().ordinal(), foundVariant, transferAmount);
                        extracted = storage.extract(foundVariant, permittedAmount, transaction);
                        if (extracted > 0)
                        {
                            variant = foundVariant;
                            amount += extracted;
                            dirty = true;
                        }
                    }
                }
            }
            transaction.commit();

            if (dirty)
            {
                parent.setChanged();
                dirty = false;
            }
        }
        super.preTick();
    }

    @Override
    public void tick()
    {
        if (amount <= 0)
            return;

        try (Transaction transaction = Transaction.openOuter())
        {
            components.clear();
            components.size(6);

            // The number of remaining transfers
            int transfers = 0;

            for (int dir = 0; dir < nodes.length; ++dir)
            {
                FluidNode node = nodes[dir];
                if (node != null)
                {
                    if (getNodeFlow(node.getNodePos(), node) >= 0 && node.getMode().canInsert())
                    {
                        components.set(dir, node);
                        ++transfers;
                    }
                }
            }

            for (int dir = 0; dir < getAdjVertices().length; ++dir)
            {
                FluidPipeVertex vertex = getAdjacent(dir);
                if (vertex != null && vertex.getTotalHeight() - this.getTotalHeight() <= 0
                        && ((SimpleFluidPipeVertex) vertex).canInsert((ServerLevel) parent.getLevel(), dir, variant, amount) > 0)
                {
                    components.set(dir, vertex);
                    ++transfers;
                }
            }

            // Randomise transfer order to reduce opportunities for fluid to get stuck in loops.
            final int[] ints = jrandom.ints(0, 6).distinct().limit(6).toArray();

            for (int dir : ints)
            {
                PipeFlowComponent component = components.get(dir);
                if (component == null)
                    continue;

                long amount1 = canOutput((ServerLevel) parent.getLevel(), dir, variant, amount);

                // Even if previous transfers failed, the remaining fluid should all be transferred.
                long transferAmount = (long) Math.min(amount1, Math.ceil(amount / (float) transfers));

                // TODO: determine toDir so that check valves work again.
                // Apparently they have been broken since April 2023 without me noticing.
                long received = component.insert(dir, 0, transferAmount, (ServerLevel) parent.getLevel(), variant, transaction);

                if (received > 0)
                {
                    amount -= received;
                    if (amount <= 0)
                        variant = FluidVariant.BLANK;
                    dirty = true;
                }

                --transfers;
            }
            transaction.commit();

            if (dirty)
            {
                parent.setChanged();
                dirty = false;
            }
        }
    }

    // Get the flow with respect to the node
    protected float getNodeFlow(NodePos pos, FluidNode node)
    {
        // Negative for influx, positive for efflux.
        float nodeFlow = node.getFlow();

        // If a node is above this vertex, the height difference should be -0.5.
        float heightFlow = -(nodeHeight(pos.face().ordinal())) - (node.getPumpHeight() - pumpHeight);

        return nodeFlow != 0 ? nodeFlow : heightFlow;
    }

    // What are you doing, stepHeight?
    protected void stepHeight()
    {
        float total = 0;
        int found = 1;

        // Sum the pump height from this vertex and adjacent ones (including nodes)
        total += getPumpHeight();

        for (int dir = 0; dir < 6; ++dir)
        {
            FluidPipeVertex vertex = getAdjacent(dir);
            FluidNode node = nodes[dir];

            if (vertex != null)
            {
                total += vertex.getPumpHeight();
                found++;
            }
            else if (node != null)
            {
                total += node.getPumpHeight();
                found++;
            }
        }

        if (found == 1)
        {
            pumpHeight = 0;
        }
        else
        {
            float f = total / found;
            pumpHeight = Math.abs(f) <= 0.01 ? 0 : f;
        }

        // Update height in nodes
        for (FluidNode node : nodes)
        {
            if (node == null) continue;

            node.setPumpHeight((pumpHeight) / 2);
        }
    }

    protected long canOutput(ServerLevel world, int outDir, FluidVariant variant, long maxAmount)
    {
        return maxAmount;
    }

    @Override
    public float getPumpHeight()
    {
        return pumpHeight;
    }

    @Override
    public String toString()
    {
        StringBuilder adj = new StringBuilder();
        for (FluidPipeVertex v : getAdjVertices()) {
            if (v != null) adj.append(System.identityHashCode(v)).append(", ");
        }
        return "Vertex@" + System.identityHashCode(this) + "{connection=" + adj + "nodes: " + Arrays.toString(nodes) + ", head:" + getTotalHeight() + "}";
    }

    @Override
    public boolean equals(Object o)
    {
        return super.equals(o);
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putLong("amount", amount);
        nbt.put("variant", variant.toNbt());

        ListTag adjacent = new ListTag();
        for (int dir = 0; dir < 6; dir++)
        {
            CompoundTag adjNbt = new CompoundTag();
            adjacent.add(adjNbt);

            FluidPipeVertex adj = getAdjVertex(dir);
            if (adj != null)
            {
                adjNbt.putLong("pos", adj.getPos());
            }

            if (nodes[dir] != null)
            {
                adjNbt.putBoolean("node", true);
            }

            // During world loading, block entities can be created, serialised then created again from the new NBT.
            // Also relevant in toInitialChunkDataNbt
            if (queuedPositions != null && queuedNodes != null)
            {
                if (queuedPositions[dir] != null)
                    adjNbt.putLong("pos", queuedPositions[dir]);

                if (queuedNodes[dir])
                    adjNbt.putBoolean("node", queuedNodes[dir]);
            }
        }

        nbt.put("adjacent", adjacent);

        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        this.amount = nbt.getLong("amount");
        this.variant = FluidVariant.fromNbt(nbt.getCompound("variant"));

        ListTag adjacent = nbt.getList("adjacent", Tag.TAG_COMPOUND);

        queuedPositions = new Long[6];
        queuedNodes = new boolean[6];
        for (int dir = 0; dir < 6; ++dir)
        {
            CompoundTag adjNbt = adjacent.getCompound(dir);

            if (adjNbt.contains("pos"))
            {
                queuedPositions[dir] = adjNbt.getLong("pos");
            }

            queuedNodes[dir] = adjNbt.contains("node");

        }
    }

    @Override
    public void erase()
    {
        for (int dir = 0; dir < 6; dir++)
        {
            FluidPipeVertex adj = getAdjVertex(dir);
            if (adj != null)
            {
                for (int outDir = 0; outDir < 6; outDir++)
                {
                    if (adj.getAdjVertex(outDir) == this)
                        adj.setAdjVertex(outDir, null);
                }
            }
        }
    }

    protected void deferredLoad()
    {
        if (queuedPositions != null)
        {
            for (int dir = 0; dir < 6; ++dir)
            {
                Long adjPos = queuedPositions[dir];

                if (adjPos == null)
                    continue;

                setAdjVertex(dir, FluidPipeVertex.LOOKUP.find(parent.getLevel(), BlockPos.of(adjPos), null));
            }

            queuedPositions = null;
        }

        if (queuedNodes != null)
        {
            for (int dir = 0; dir < 6; ++dir)
            {
                if (queuedNodes[dir])
                {
                    FluidNode node = parent.getNode(Direction.values()[dir]);
                    if (node != null)
                    {
                        nodes[dir] = node;
                    }
                }
            }
            queuedNodes = null;

            invalidateCanSimplify();
        }
    }
}
