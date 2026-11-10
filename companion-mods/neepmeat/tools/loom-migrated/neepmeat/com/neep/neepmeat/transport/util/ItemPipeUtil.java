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

package com.neep.neepmeat.transport.util;

import com.google.common.collect.Iterables;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.meatlib.util.PipeConnections;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.machine.ItemPumpBlock;
import com.neep.neepmeat.transport.block.item_transport.machine.PipeItemMemory;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;
import com.neep.neepmeat.transport.item_network.RetrievalTarget;
import com.neep.neepmeat.transport.item_network.ShittyRoutingListener;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public class ItemPipeUtil
{
    public static class EjectOperation
    {
        private final ServerLevel world;
        private final BlockPos pos;
        private final Direction out;
        @Nullable private final PipeItemMemory memory;
        private PipeRouteFinder pathfinding;
        private final Predicate<Storage<ItemVariant>> exclude = s -> true;

        private long amount = 0;

        public static EjectOperation of(Level world, BlockPos pos, Direction out, @Nullable PipeItemMemory memory)
        {
            return new EjectOperation(world, pos, out, memory);
        }

        public static EjectOperation of(Level world, BlockPos pos, Direction out)
        {
            return of(world, pos, out, null);
        }

        private EjectOperation(Level world, BlockPos pos, Direction out, @Nullable PipeItemMemory memory)
        {
            this.world = (ServerLevel) world;
            this.pos = pos;
            this.out = out;
            this.memory = memory;

            this.pathfinding = new PipeRouteFinder(this.world, pos.relative(out));
        }

        public EjectOperation useRouting(boolean route)
        {
            if (!route)
                this.pathfinding = null;
            else
                this.pathfinding = new PipeRouteFinder(this.world, pos.relative(out));

            return this;
        }

        /// Handles the movement of an {@link ItemInPipe} to an adjacent block.
        public EjectOperation pipeToAny(ItemInPipe item, TransactionContext transaction)
        {
            this.amount = pipeToAnyInternal(item, transaction);
            return this;
        }

        /// Handles the movement of any item and amount to an adjacent block.
        public EjectOperation stackToAny(ItemVariant resource, long amount, TransactionContext transaction)
        {
            this.amount = stackToAnyInternal(resource, amount, transaction);
            return this;
        }

        public long amount()
        {
            return amount;
        }

        private long pipeToAnyInternal(ItemInPipe item, TransactionContext transaction)
        {
            BlockPos toPos = pos.relative(out);
            BlockState toState = world.getBlockState(toPos);
            Block toBlock = toState.getBlock();

            long amountInserted = 0;
            Storage<ItemVariant> storage;
            if (toBlock instanceof ItemPipe pipe)
            {
                amountInserted = itemToPipe(item, pipe, world, toPos, toState, out, pathfinding, memory, exclude, transaction);
            }
            else if (canDumpInto(world, toPos, toState))
            {
                amountInserted = itemToWorld(item, 0.2, item.speed, world, toPos, out, transaction);
            }
            else if ((storage = ItemStorage.SIDED.find(world, toPos, out.getOpposite())) != null)
            {
                amountInserted = itemToStorage(item, storage, transaction);
            }

            // TODO: is this condition necessary?
            if (amountInserted != item.amount())
            {
                item.decrement((int) amountInserted);
            }

            return amountInserted;
        }

        private int stackToAnyInternal(ItemVariant variant, long amount, TransactionContext transaction)
        {
            if (variant.isBlank() || amount == 0)
                return 0;

            BlockPos offset = pos.relative(out);
            BlockState facingState = world.getBlockState(offset);
            Storage<ItemVariant> storage;

            long transferred = 0;
            try (Transaction nested = transaction.openNested())
            {
                if (canDumpInto(world, offset, facingState))
                {
                    transferred = itemToWorld(new ItemInPipe(variant, (int) amount), 0.2, 0.05f, world, offset, out, nested);
                    nested.commit();
                }
                else if (facingState.getBlock() instanceof ItemPipe itemPipe)
                {
                    transferred = itemToPipe(new ItemInPipe(new ResourceAmount<>(variant, amount), world.getGameTime()),
                            itemPipe, world, offset, facingState, out, pathfinding, memory, exclude, nested);
                    nested.commit();
                }
                else if ((storage = ItemStorage.SIDED.find(world, offset, out.getOpposite())) != null)
                {
                    transferred = storage.insert(variant, amount, nested);
                    nested.commit();
                }
                else nested.abort();
            }
            return (int) transferred;
        }
    }

    /** Ejects the entire contents of the given storage into a pipe network of the world.
     * @return Whether more than zero items were successfully ejected
     */
    public static boolean storageToAny(ServerLevel world, Storage<ItemVariant> storage, BlockPos pos, Direction facing, TransactionContext transaction)
    {
        for (StorageView<ItemVariant> view : storage)
        {
            try (Transaction inner = transaction.openNested())
            {
                if (view.isResourceBlank())
                {
                    inner.abort();
                    continue;
                }

                long ejected = EjectOperation.of(world, pos, facing).stackToAny(view.getResource(), view.getAmount(), inner).amount();
                long extracted = view.extract(view.getResource(), ejected, inner);
                if (ejected == extracted)
                {
                    inner.commit();
                    return ejected != 0;
                }
                else inner.abort();
            }
        }
        return false;
    }

    /**
     * @param item Pipe item
     * @param storage The ItemVariant storage to be inserted into
     * @return The number of items that were inserted
     */
    public static long itemToStorage(ItemInPipe item, Storage<ItemVariant> storage, TransactionContext transaction)
    {
        try (Transaction nested = transaction.openNested())
        {
            long transferred = storage.insert(item.resource(), item.amount(), nested);
            if (transferred > 0)
            {
                nested.commit();
                item.decrement((int) transferred);
                return transferred;
            }
            nested.abort();
        }
        return 0;
    }

    /** Spawns an item entity in the world with a given speed.
     */
    public static long itemToWorld(ItemInPipe item, double offset, float speed, Level world, BlockPos toPos, Direction out, TransactionContext transaction)
    {
        transaction.addOuterCloseCallback(r ->
        {
            if (!r.wasCommitted())
                return;

            ItemStack stack = item.toItemStack();
            item.zero();

            ItemEntity itemEntity = new ItemEntity(world,
                    toPos.getX() + 0.5 - offset * out.getStepX(),
                    toPos.getY() + 0.2 - offset * out.getStepY(),
                    toPos.getZ() + 0.5 - offset * out.getStepZ(),
                    stack,
                    out.getStepX() * speed, out.getStepY() * speed, out.getStepZ() * speed);
            world.addFreshEntity(itemEntity);
        });
        return item.amount();
    }

    public static long itemToPipe(ItemInPipe item, ItemPipe pipe, ServerLevel world, BlockPos toPos, BlockState toState, Direction out, @Nullable PipeRouteFinder pathfinding, @Nullable PipeItemMemory memory, Predicate<Storage<ItemVariant>> exclude, TransactionContext transaction)
    {
        long amountInserted = 0;
        long maxAmount = item.amount();

        if (pipe.canItemEnter(item.resource(), item.amount(), world, toPos, toState, out.getOpposite(), item.hasRoute()))
        {
            if (pathfinding != null)
            {
                // Limit maximum transfer amount to this
                var result = pathfinding.run(item.toResourceAmount(), exclude, out.getOpposite(), transaction);
//                var check = new PipePathfinding.Operation(world, toPos)
//                        .run(item.toResourceAmount(), exclude, out.getOpposite(), transaction);

                if (memory != null)
                {
                    int memoryAmount = memory.get(item.resource());
                    maxAmount = Math.min(maxAmount, result.maxAmount() - memoryAmount);
                }
                else
                    maxAmount = Math.min(maxAmount, result.maxAmount());

                // TODO: Implement round-robin and use the route
//                item.setRoute(result.firstRoute());
            }

            if (maxAmount != 0)
            {
                ItemInPipe newItem = item.split((int) maxAmount);

                if (memory != null)
                    memory.register(newItem);

                amountInserted = pipe.insert(world, toPos, toState, out.getOpposite(), newItem, transaction);
            }
        }
        return amountInserted;
    }

    /// Handles transfer of an {@link ItemInPipe} into a {@link ItemPipe}
    public static long itemToPipe(ItemInPipe item, ItemPipe pipe, ServerLevel world, BlockPos toPos, BlockState toState, Direction out, TransactionContext transaction)
    {
        long amountInserted = 0;
        long maxAmount = item.amount();

        if (pipe.canItemEnter(item.resource(), item.amount(), world, toPos, toState, out.getOpposite(), item.hasRoute()))
        {
            if (maxAmount != 0)
            {
                ItemInPipe newItem = item.split((int) maxAmount);
                amountInserted = pipe.insert(world, toPos, toState, out.getOpposite(), newItem, transaction);
            }
        }
        return amountInserted;
    }

    public static void bounce(ItemInPipe item, Level world, BlockState state)
    {
        Direction out;
        Direction in = item.out;

        PipeConnections connections = ((ItemPipe) state.getBlock()).getConnections(state, in);

        var rand = world.getRandom();
        if (!connections.isEmpty())
        {
            out = Iterables.get(connections, rand.nextInt(connections.size()));
        }
        else
        {
            out = in;
        }

        item.reset(in, out, world.getGameTime());
    }

    // TODO: Fix
    public static List<RetrievalTarget<ItemVariant>> floodSearch(ShittyRoutingListener listener, BlockPos startPos, Direction face, Level world, int depth)
    {
        Deque<BlockPos> pipeQueue = new ArrayDeque<>();
        List<BlockPos> nextSet = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        List<ItemPipeBlockEntity> relevantPipes = new ArrayList<>();
        List<RetrievalTarget<ItemVariant>> output = new ArrayList<>();

        pipeQueue.add(startPos.relative(face));
        visited.add(startPos.relative(face));

        for (int i = 0; i < depth; ++i)
        {
            nextSet.clear();
            while (!pipeQueue.isEmpty())
            {
                BlockPos current = pipeQueue.poll();
                BlockState currentState = world.getBlockState(current);

                if (world.getBlockEntity(current) instanceof ItemPipeBlockEntity be)
                {
                    relevantPipes.add(be);
                }

                for (Direction direction : Direction.values())
                {
                    BlockPos next = current.relative(direction);
                    BlockState nextState = world.getBlockState(next);

                    if (ItemPipe.isConnectedIn(world, current, currentState, direction) && !visited.contains(next))
                    {
                        visited.add(next);

                        // Check that target is a pipe
                        if (nextState.getBlock() instanceof ItemPipe &&
                                !(nextState.getBlock() instanceof ItemPumpBlock))
                        {
                            // Next pipe is connected to current pipe
                            if (ItemPipe.isConnectedIn(world, next, nextState, direction.getOpposite()))
                            {
                                nextSet.add(next);
                            }
                        }
                        if (ItemStorage.SIDED.find(world, next, direction.getOpposite()) != null)
                        {
                            BlockApiCache<Storage<ItemVariant>, Direction> cache = BlockApiCache.create(ItemStorage.SIDED, (ServerLevel) world, next);
                            output.add(new RetrievalTarget<>(cache, direction.getOpposite()));
                        }
                    }
                }
            }
            pipeQueue.addAll(nextSet);

        }

        for (var pipe : relevantPipes)
        {
            pipe.getCallbacks().addListener(listener);
        }

        return output;
    }

    public static boolean canDumpInto(Level world, BlockPos pos, BlockState state)
    {
        return !state.blocksMotion();
//        return !state.isSolidBlock(world, pos);
    }
}