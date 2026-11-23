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

import com.neep.meatlib.api.event.InitialTicks;
import com.neep.neepmeat.api.FluidPump;
import com.neep.neepmeat.transport.fluid_network.FluidPipeVertex;
import com.neep.neepmeat.transport.fluid_network.PipeFlowComponent;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import com.neep.meatlib.transfer.FluidStorage;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

/*
    An interface for fluid networks associated with a block and a direction
 */
@SuppressWarnings("UnstableApiUsage")
public class FluidNode implements PipeFlowComponent
{
    private final NodePos pos;

    @Nullable private FluidPump pump;
    public boolean hasPump;

    @Nullable private BlockApiCache<Storage<FluidVariant>, Direction> storage;
    public boolean isStorage;

    public boolean needsDeferredLoading;

    public FluidNode(NodePos nodePos, ServerLevel world)
    {
        this.pos = nodePos;
        this.isStorage = findStorage(world);
        this.hasPump = findPump(world);
    }

    // For deferred loading only.
    protected FluidNode(NodePos pos, ServerLevel world, boolean isStorage, boolean hasPump)
    {
        this.pos = pos;
        this.storage = null;
        this.pump = null;
        this.isStorage = isStorage;
        this.hasPump = hasPump;
        this.needsDeferredLoading = true;

//        FluidNodeManager.getInstance(world).queueNode(this);
        InitialTicks.getInstance(world).queue(this::loadDeferred);
    }

    @Override
    public String toString()
    {
        return "\n" + this.pos.toString() + " " + pos.face() + " storage: " + storage;
    }

    // Load a node from NBT data
    public static FluidNode fromNbt(CompoundTag nbt, ServerLevel world)
    {
        CompoundTag posNbt = nbt.getCompound("pos");
        NodePos pos = NodePos.fromNbt(posNbt);
        boolean isStorage = nbt.getBoolean("is_storage");
        boolean hasPump = nbt.getBoolean("hasPump");

        return new FluidNode(pos, world, isStorage, hasPump);
    }

    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.put("pos", pos.toNbt(new CompoundTag()));
        nbt.putBoolean("is_storage", isStorage);
        nbt.putBoolean("hasPump", hasPump);
        return nbt;
    }

    public void loadDeferred(ServerLevel world)
    {
        load(world);

        // Invalid state that should never arise (but does)
        if (hasPump && pump == null)
            hasPump = false;

        needsDeferredLoading = false;
    }

    private void load(ServerLevel world)
    {
        if (!(needsDeferredLoading && storage == null) || !world.getServer().isSameThread())
        {
            return;
        }

        findStorage(world);
        findPump(world);
    }

    public boolean findPump(ServerLevel world)
    {
        FluidPump pump = FluidPump.SIDED.find(world, pos.facingBlock(), pos.face().getOpposite());
        if (pump != null)
        {
            this.pump = pump;
            return true;
        }
        return false;
    }

    public boolean findStorage(ServerLevel world)
    {
        storage = BlockApiCache.create(FluidStorage.SIDED, world, pos.facingBlock());
        return storage.find(pos.face().getOpposite()) != null;
    }

    public NodePos getNodePos()
    {
        return pos;
    }

    public float getFlow()
    {
        if (pump != null)
            return pump.getFlow();

        return getMode().getFlow();
    }

    public Storage<FluidVariant> getStorage(ServerLevel world)
    {
        if (storage == null)
            load(world);

        // Invalid state that should never arise
        if (storage == null)
            return Storage.empty();

        return storage.find(pos.face().getOpposite());
    }

    @Nullable
    public FluidPump getPump()
    {
        return pump;
    }

    public AcceptorModes getMode()
    {
        if (pump != null)
            return pump.getMode();

        return AcceptorModes.INSERT_EXTRACT;
    }

    // This section is another level of jank to add to the already numerous strata.
    protected float pumpHeight;

    public void setPumpHeight(float height)
    {
        this.pumpHeight = height;
    }

    public float getPumpHeight()
    {
        if (pump == null || !pump.getMode().isDriving())
            return pumpHeight;

        // Override if driving
        return pump.getPumpHeight();
    }

    @Override
    public long insert(int fromDir, int toDir, long maxAmount, ServerLevel world, FluidVariant variant, TransactionContext transaction)
    {
        if (!variant.isBlank() && getStorage(world) != null)
        {
            return getStorage(world).insert(variant, maxAmount, transaction);
        }
        return 0;
    }

    @Override
    public int getConnectionDir(FluidPipeVertex from)
    {
        return pos.face().getOpposite().ordinal();
    }
}
