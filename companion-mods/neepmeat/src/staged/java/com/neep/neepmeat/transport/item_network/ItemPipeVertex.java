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

import com.google.common.annotations.VisibleForTesting;
import com.neep.meatlib.util.graph.PipeVertex;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.ResourceAmount;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface ItemPipeVertex extends PipeVertex, ItemPipeGraphHelper
{
//    BlockApiLookup<ItemPipeVertex, Void> LOOKUP = BlockApiLookup.get(new Identifier(
//            NeepMeat.NAMESPACE, "item_pipe_vertex"), ItemPipeVertex.class, void.class);

    ItemPipeVertex NULL = new ItemPipeVertex()
    {
        @Override
        public ItemPipeVertex[] getAdjVertices()
        {
            return new ItemPipeVertex[6];
        }

        @Override
        public int[] getSortedIndices()
        {
            return new int[6];
        }

        @Override
        public boolean canSimplify()
        {
            return true;
        }

        @Override
        public void setAdjVertex(int dir, PipeVertex vertex)
        {

        }

        @Override
        public void setAdjVertex(int dir, ItemPipeVertex vertex)
        {

        }

        @Override
        public BlockPos getPos()
        {
            return BlockPos.ZERO;
        }

        @Override
        public Direction getDirection(ItemPipeVertex vertex)
        {
            return Direction.NORTH;
        }

        @Override
        public @Nullable Storage<ItemVariant> getStorage(Direction direction)
        {
            return null;
        }

        @Override
        public boolean prioritise()
        {
            return false;
        }

        @Override
        public int getCurrentOutput(Direction inFace)
        {
            return -1;
        }

        @Override
        public void nextOutput(Direction inFace)
        {

        }

        @Override
        public void clearEdges()
        {

        }

        @Override
        public ItemPipe getPipe()
        {
            return null;
        }

        @Override
        public boolean canItemEnter(ItemVariant variant, long amount, Direction enterDirection, boolean routed)
        {
            return false;
        }

        @Override
        public boolean canItemLeave(ResourceAmount<ItemVariant> resource, Direction face, boolean routed)
        {
            return false;
        }

        @Override
        public boolean isConnected(Direction face)
        {
            return false;
        }

        @Override
        public void prepareForRemoval(BlockState state, BlockState newState)
        {

        }

        @Override
        public void updateConnectionChange(BlockState state, BlockState newState)
        {

        }

        @Override
        public void updateHiddenConnections(BlockState state)
        {

        }
    };

    @Nullable
    static ItemPipeVertex find(ServerLevel world, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ItemPipe pipe)
        {
            return (ItemPipeVertex) pipe.getPipeVertex(world, pos, state);
        }

        return null;
    }

    int[] getSortedIndices();

    boolean canSimplify();

    ItemPipeVertex[] getAdjVertices();
    void setAdjVertex(int dir, PipeVertex vertex);
    void setAdjVertex(int dir, ItemPipeVertex vertex);
    void clearEdges();

    default ItemPipeVertex getAdjVertex(int dir)
    {
        return getAdjVertices()[dir];
    }

    @Nullable
    ItemPipe getPipe();

    // Delegates to ItemPipe.
    // Allows the vertex to use cached block states and block entities
    boolean canItemEnter(ItemVariant variant, long amount, Direction enterDirection, boolean routed);
    boolean canItemLeave(ResourceAmount<ItemVariant> resource, Direction face, boolean routed);
    boolean isConnected(Direction face);

    @VisibleForTesting
    BlockPos getPos();

    Direction getDirection(ItemPipeVertex vertex);

    @Nullable Storage<ItemVariant> getStorage(Direction direction);

    // TODO: convert to configurable int priority
    boolean prioritise();

    int getCurrentOutput(Direction inFace);
    void nextOutput(Direction inFace);
}
