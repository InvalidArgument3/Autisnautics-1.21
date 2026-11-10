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

package com.neep.neepmeat.transport.fluid_network;

import com.neep.neepmeat.transport.api.FluidTransportConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.server.level.ServerLevel;

@SuppressWarnings("UnstableApiUsage")
public abstract class SimpleFluidPipeVertex extends SnapshotParticipant<ResourceAmount<FluidVariant>> implements FluidPipeVertex
{
//    protected final List<Direction> connections = new ArrayList<>();
    private final FluidPipeVertex[] adjacentVertices = new FluidPipeVertex[6];
    protected float pumpHeight = 0;
    protected float height;

    protected long amount;
    protected long oldAmount;

    protected final long pos;
    protected FluidVariant variant = FluidVariant.blank();

    public SimpleFluidPipeVertex(long pos)
    {
        this.pos = pos;
    }

    public long getCapacity()
    {
        return FluidTransportConstants.INSTANCE.maxTransfer();
    }

    public long getAmount()
    {
        return amount;
    }

    public FluidVariant getVariant()
    {
        return variant;
    }

    @Override
    protected ResourceAmount<FluidVariant> createSnapshot()
    {
        return new ResourceAmount<>(variant, amount);
    }

    @Override
    protected void readSnapshot(ResourceAmount<FluidVariant> snapshot)
    {
        this.variant = snapshot.resource();
        this.amount = snapshot.amount();
    }

    public void tick()
    {
    }

    @Override
    public void preTick()
    {
        FluidPipeVertex.super.preTick();
        oldAmount = amount;
    }

    public long insert(int fromDir, int toDir, long maxAmount, ServerLevel world, FluidVariant insertVariant, TransactionContext transaction)
    {
        long remaining = getCapacity() - amount;
        if ((!insertVariant.isBlank() && (variant.isBlank() || insertVariant.equals(variant))) && remaining > 0)
        {
            updateSnapshots(transaction);

            long permittedAmount = Math.min(canInsert(world, toDir, insertVariant, maxAmount), remaining);

            if (permittedAmount > 0)
            {
                this.amount += permittedAmount;
                variant = insertVariant;
                return permittedAmount;
            }
            return 0;
        }
        return 0;
    }

    @Override
    public int getConnectionDir(FluidPipeVertex from)
    {
        getAdjVertices();
        for (int i = 0; i < adjacentVertices.length; ++i)
        {
            if (from.equals(adjacentVertices[i])) return i;
        }
        return 0;
    }

    @Override
    public String toString()
    {
        StringBuilder adj = new StringBuilder();
        for (FluidPipeVertex v : adjacentVertices)
        {
            if (v != null) adj.append(System.identityHashCode(v)).append(", ");
        }
        return "Vertex@"+System.identityHashCode(this)+"{connection=" + adj + "head:" + getTotalHeight() + "}";
    }

    @Override
    public boolean equals(Object o)
    {
        // Not sure why but I assume it was important
        if (o == this)
        {
            return true;
        }
//        if (o instanceof SimplePipeVertex vertex)
//        {
//            return Arrays.equals(adjacentVertices, vertex.getAdjVertices())
//                    && pressureHead == vertex.pressureHead
//                    && elevationHead == vertex.elevationHead
//                    && amount == vertex.amount;
//        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return System.identityHashCode(this);
    }

//    public void putAdjacent(Direction direction, PipeVertex vertex)
//    {
//        adjacentVertices[direction.ordinal()] = vertex;
//    }
//
//    public PipeVertex getAdjacent(Direction direction)
//    {
//        return getAdjacent(direction.ordinal());
//    }

    public FluidPipeVertex getAdjacent(int dir)
    {
        return adjacentVertices[dir];
    }

    public FluidPipeVertex[] getAdjVertices()
    {
        return adjacentVertices;
    }

    public void setHeight(float value)
    {
        this.height = value;
    }

    @Override
    public long getPos()
    {
        return pos;
    }

    public float getTotalHeight()
    {
        return pumpHeight + getHeight();
    }

    protected float getHeight()
    {
        return FluidTransportConstants.INSTANCE.pipeGravityEnabled() ? height : 0;
    }

    protected int numEdges()
    {
        int edges = 0;
        for (FluidPipeVertex pipeNetVertex : getAdjVertices())
        {
            if (pipeNetVertex != null) ++edges;
        }
        return edges;
    }

    @Override
    public boolean canSimplify()
    {
        return numEdges() <= 2;
    }

    @Override
    public void reset()
    {
        pumpHeight = 0;
        clearEdges();
    }

    protected void clearEdges()
    {
        for (int i = 0; i < 6; ++i)
        {
            FluidPipeVertex adj = getAdjacent(i);
            if (adj == null) continue;

            for (int j = 0; j < 6; ++j)
            {
                if (adj.getAdjVertex(j) == this)
                {
                    adj.setAdjVertex(j, null);
                }
            }

            setAdjVertex(i, null);
        }
    }

    public long canInsert(ServerLevel world, int inDir, FluidVariant variant, long maxAmount)
    {
        return Math.max(0, Math.min(maxAmount, getCapacity() - getAmount()));
    }
}
