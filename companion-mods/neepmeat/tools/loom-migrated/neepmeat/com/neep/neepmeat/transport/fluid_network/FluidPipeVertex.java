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

import com.neep.meatlib.util.graph.PipeVertex;
import com.neep.neepmeat.NeepMeat;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface FluidPipeVertex extends PipeFlowComponent, PipeVertex
{
    void tick();

    default void preTick() {}

    FluidPipeVertex[] getAdjVertices();

    default FluidPipeVertex getAdjVertex(int dir)
    {
        return getAdjVertices()[dir];
    }

    @Override
    default void setAdjVertex(int dir, PipeVertex vertex)
    {
        setAdjVertex(dir, (FluidPipeVertex) vertex);
    }

    default void setAdjVertex(int dir, FluidPipeVertex vertex)
    {
        getAdjVertices()[dir] = vertex;
    }

    boolean canSimplify();

    void tickDeferredLoad();

    void reset();

    void updateNodes(ServerLevel world, BlockPos pos, BlockState state);

    float getTotalHeight();
    void setHeight(float value);

    long getPos();

    float getPumpHeight();

    void erase();

    BlockApiLookup<FluidPipeVertex, Void> LOOKUP = BlockApiLookup.get(
            new ResourceLocation(NeepMeat.NAMESPACE, "pipe_vertex"),
            FluidPipeVertex.class, Void.class
    );
}
