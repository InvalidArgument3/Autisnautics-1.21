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

import com.neep.neepmeat.util.PipeGraphUtil;
import com.neep.neepmeat.transport.api.PipeCache;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import com.neep.neepmeat.transport.util.PipePathfinding;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// This might need to be removed or renamed.
// It no never cached pipes, and will never do so again.
public class PipeCacheImpl implements PipeCache
{
    protected final ServerLevel world;

    public PipeCacheImpl(ServerLevel world)
    {
        this.world = world;
    }

    @Override
    public boolean retrieve(BlockPos to, Direction in, ItemVariant variant, long amount, TransactionContext transaction)
    {
        return false;
    }

    @Override
    public long eject(BlockPos from, Direction out, ItemVariant variant, long amount, TransactionContext transaction)
    {
        return 0;
    }

    @Override
    public long route(BlockPos from, Direction in, BlockPos to, Direction out, ItemVariant variant, long amount, TransactionContext transaction)
    {
        ItemInPipe item = new ItemInPipe(null, null, variant, (int) amount, world.getGameTime());

        BlockPos fromPos = from.relative(in); // First pipe
        ItemRoute route = findPath(new ResourceAmount<>(variant, amount), fromPos, to, out);

        item.setRoute(route);

        return ItemPipeUtil.EjectOperation.of(world, from, in)
                .useRouting(false)
                .pipeToAny(item, transaction)
                .amount();
    }

    @Override
    public long route(BlockPos from, Direction fromFace, ItemRoute route, ItemVariant variant, long amount, TransactionContext transaction)
    {
        ItemInPipe item = new ItemInPipe(null, null, variant, (int) amount, world.getGameTime());

        item.setRoute(route);
        return ItemPipeUtil.EjectOperation.of(world, from, fromFace)
                .useRouting(false)
                .pipeToAny(item, transaction)
                .amount();
    }

    /**
     * @param item Desired item variant and amount
     * @param startPos Position of the input storage
     * @param endPos Position of the output pipe
     * @param endFace Output direction of the output pipe
     * @return The route
     */
    public ItemRoute findPath(ResourceAmount<ItemVariant> item, BlockPos startPos, BlockPos endPos, Direction endFace)
    {
        BlockState destinationState = world.getBlockState(endPos);
        if (destinationState.getBlock() instanceof ItemPipe destPipe)
        {
            // Handle situations where the target pipe vertex has been collapsed
            var destinationVertex = destPipe.getPipeVertex(world, endPos, destinationState);
            if (destinationVertex == null || destinationVertex.canSimplify())
            {
                var connections = destPipe.getConnections(destinationState, endFace);
                if (!connections.isEmpty())
                {
                    var pair = PipeGraphUtil.findNextVertex(world, endPos, endFace, ItemPipeVertex::find, ItemPipe::findItemPipe);
                    if (pair != null)
                    {
                        endPos = pair.first().getPos();
                        endFace = pair.second().getOpposite();
                    }
                    else
                    {
                        return ItemRoute.EMPTY;
                    }
                }
                else
                {
                    return ItemRoute.EMPTY;
                }
            }
        }

        var startVertex = ItemPipeVertex.find(world, startPos);

        if (startVertex == null)
            return ItemRoute.EMPTY; // This is possible if something is incorrectly set up

        Map<ItemPipeVertex, PipeRouteFinder.Node> vertexToNode = new HashMap<>();
        Set<ItemPipeVertex> visited = new HashSet<>();
        it.unimi.dsi.fastutil.PriorityQueue<PipeRouteFinder.Node> frontier = new ObjectHeapPriorityQueue<>();

        var startNode = vertexToNode.computeIfAbsent(startVertex, PipeRouteFinder.Node::new);
        visited.add(startVertex);
        frontier.enqueue(startNode);

        while (!frontier.isEmpty())
        {
            var node = frontier.dequeue();

            if (node.vertex.getPos().equals(endPos))
            {
                return PipePathfinding.makeRoute(world, node, endFace);
            }

            for (int dir = 0; dir < 6; dir++)
            {
                Direction face = Direction.values()[dir];

                if (!node.vertex.canItemLeave(item, face, true))
                    continue;

                var adj = node.vertex.getAdjVertex(dir);
                if (adj != null)
                {
                    var child = vertexToNode.computeIfAbsent(adj, PipeRouteFinder.Node::new);

                    if (!visited.contains(adj))
                    {
                        Direction enterDirection = adj.getDirection(node.vertex);
                        if (adj.canItemEnter(item.resource(), item.amount(), enterDirection, true))
                        {
                            // Direction matters. A pipe could have invalid and valid directions.
                            // The pipe block should only be marked as visited when a valid direction has been checked.
                            visited.add(adj);

                            child.cost = node.cost;
                            if (child.vertex.prioritise())
                                child.cost -= 1;

                            child.parent = node;
                            child.parentOutFace = face;
//                            child.inFace = inFace;
                            frontier.enqueue(child);
                        }
                    }
                }
            }
        }

        return ItemRoute.EMPTY;
    }
}
