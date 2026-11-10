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

package com.neep.neepmeat.transport.block.fluid_transport.machine;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.meatlib.util.graph.ConnectablePipe;
import com.neep.neepmeat.util.PipeGraphUtil;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.api.pipe.FluidPipe;
import com.neep.neepmeat.transport.fluid_network.FluidNodeHolder;
import com.neep.neepmeat.transport.fluid_network.FluidNodeHelper;
import com.neep.neepmeat.transport.fluid_network.FluidPipeVertex;
import com.neep.neepmeat.transport.fluid_network.node.BlockFluidPipeVertex;
import com.neep.neepmeat.transport.fluid_network.node.FluidNode;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FluidPipeBlockEntity<T extends FluidPipeVertex & NbtSerialisable> extends BlockEntity implements FluidNodeHolder
{
    protected boolean replaced = false;
    public CompoundTag queuedNbt;
    protected final T vertex;
    protected final PipeConstructor<T> constructor;

    protected final List<FluidNode> fluidNodes = new ObjectArrayList<>(Collections.nCopies(6, null));

    public FluidPipeBlockEntity(BlockPos pos, BlockState state, PipeConstructor<T> constructor)
    {
        this(NMBlockEntities.FLUID_PIPE, pos, state, constructor);
    }

    public FluidPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PipeConstructor<T> constructor)
    {
        super(type, pos, state);
        this.vertex = constructor.create(this);
        this.constructor = constructor;
    }

    public static Optional<FluidPipeBlockEntity<?>> find(Level world, BlockPos pos)
    {
        if (world.getBlockEntity(pos) instanceof FluidPipeBlockEntity<?> be)
        {
            return Optional.of(be);
        }
        return Optional.empty();
    }

    @Override
    public void clearNodes()
    {
        for (int i = 0; i < 6; i++)
            fluidNodes.set(i, null);
    }

    @Override
    public boolean removeNode(Direction face)
    {
        if (fluidNodes.get(face.ordinal()) != null)
        {
            fluidNodes.set(face.ordinal(), null);
            return true;
        }

        return false;
    }

    @Override
    public @Nullable FluidNode getNode(Direction face)
    {
        return fluidNodes.get(face.ordinal());
    }

    @Override
    public void putNode(Direction face, FluidNode node)
    {
        fluidNodes.set(face.ordinal(), node);
    }

    public void updateHiddenConnections(BlockState newState)
    {
        FluidPipe pipe = (FluidPipe) newState.getBlock();

        vertex.reset();
        vertex.updateNodes((ServerLevel) level, worldPosition, newState);

        int connections = pipe.countConnections(newState);
        if (connections > 2 || !vertex.canSimplify())
        {
            findAdjacent(pipe);
        }
        else if (connections == 2)
        {
            PipeGraphUtil.linkVertices((ServerLevel) level, worldPosition, getBlockState(), pipe, (world, pos) -> FluidPipeVertex.LOOKUP.find(world, pos, null), FluidPipe::findFluidPipe);
        }
        setChanged();
    }

    public void updateConnectionChange(BlockState state, BlockState newState)
    {
        FluidPipe pipe = (FluidPipe) newState.getBlock();

        int connections = pipe.countConnections(newState);

        // If this is a newly created pipe end, we need to break the previous connection.
        if (vertex.canSimplify() && connections == 1)
        {
            vertex.reset();
            vertex.updateNodes((ServerLevel) level, worldPosition, newState);

            // Using a loop to be safe, but this should only happen once.
            for (Direction direction : pipe.getConnections(getBlockState()))
            {
                var adjacent = findNextVertex(worldPosition, direction);
                if (adjacent != null)
                {
                    adjacent.first().setAdjVertex(adjacent.second().ordinal(), null);
                }
            }
        }
        else
        {
            updateHiddenConnections(newState);
        }
    }

    // For use if this pipe is a vertex. Performs a BFS at each connection to find the closest vertices.
    private void findAdjacent(FluidPipe pipe)
    {
        for (Direction direction : pipe.getConnections(getBlockState()))
        {
            var adjacent = findNextVertex(worldPosition, direction);
            if (adjacent != null)
            {
                vertex.setAdjVertex(direction.get3DDataValue(), adjacent.first());
                adjacent.first().setAdjVertex(adjacent.second().get3DDataValue(), vertex);
            }
        }
    }

    private Pair<FluidPipeVertex, Direction> findNextVertex(BlockPos pos, Direction out)
    {
        return PipeGraphUtil.findNextVertex((ServerLevel) level, pos, out, (w, p) -> FluidPipeVertex.LOOKUP.find(w, p, null), FluidPipe::findFluidPipe);
    }

    @Override
    public void setChanged()
    {
        if (level != null)
            level.blockEntityChanged(worldPosition);
    }

    @Override
    public void setBlockState(BlockState state)
    {
        super.setBlockState(state);

        // Uuuuuuugh
        if (vertex instanceof BlockFluidPipeVertex blockPipeVertex)
        {
            // But it's for optimisation!
            blockPipeVertex.invalidateCanSimplify();
        }
    }

    public void prepareForRemoval(BlockState state, BlockState newState)
    {
        markReplaced();

        PipeGraphUtil.eraseHiddenFromNeighbours((ServerLevel) level, worldPosition, state, (ConnectablePipe) getBlockState().getBlock(), FluidPipe::findFluidPipe);
    }

    @Override
    public void setLevel(Level world)
    {
        super.setLevel(world);
        if (getLevel() instanceof ServerLevel serverWorld && world.getServer().isSameThread())
        {
            if (queuedNbt != null)
            {
                FluidNodeHelper.readNodes(this, queuedNbt, serverWorld);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);

        FluidNodeHelper.writeNodes(this, nbt);

        nbt.put("vertex", vertex.writeNbt(new CompoundTag()));
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        queuedNbt = nbt.copy();

        vertex.readNbt(nbt.getCompound("vertex"));
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
    }

    public void markReplaced()
    {
        replaced = true;
    }

    public T getPipeVertex()
    {
        return vertex;
    }

    public void onUnload(ServerLevel world)
    {
    }

    public void onLoad(ServerLevel world)
    {
    }

    protected void onRemove(ServerLevel world)
    {
        vertex.erase();
    }

    @FunctionalInterface
    public interface PipeConstructor<T extends FluidPipeVertex & NbtSerialisable>
    {
        T create(FluidPipeBlockEntity<T> parent);
    }

    static
    {
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) ->
        {
            if (blockEntity instanceof FluidPipeBlockEntity<?> be)
            {
                if (!be.replaced)
                {
                    be.onUnload(world);
                }
                else
                {
                    be.onRemove(world);
                    be.replaced = false;
                }
            }
        });
    }

    public void tick()
    {
        vertex.tickDeferredLoad();
        if (!vertex.canSimplify())
        {
            vertex.preTick();
            vertex.tick();
        }

    }
}
