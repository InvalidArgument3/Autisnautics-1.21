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

package com.neep.neepmeat.transport.api.item_network;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.machine.fabricator.RecipeMatching;
import com.neep.neepmeat.transport.block.item_transport.RoutingNetworkImpl;
import com.neep.neepmeat.transport.fluid_network.node.NodePos;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface RoutablePipe
{
    BlockApiLookup<RoutablePipe, Direction> LOOKUP  = BlockApiLookup.get(new ResourceLocation(NeepMeat.NAMESPACE, "routable_pipe"), RoutablePipe.class, Direction.class);

    default int outPriority()
    {
        return 0;
    }

    default int inPriority()
    {
        return 0;
    }

    Result request(Predicate<ItemVariant> predicate, long amount, NodePos fromPos, RoutingNetwork.RequestContext context, TransactionContext transaction);

    long request2(Predicate<ItemVariant> predicate, long amount, NodePos dest, TransactionContext transaction);

    Stream<StorageView<ItemVariant>> getAvailable(TransactionContext transaction);

    default Stream<Craftable> getCraftable(TransactionContext transaction) { return Stream.empty(); }

    BlockPos getRoutablePipePos();

    default DeferredResult deferredUpdate(RoutingNetwork.DeferredRequest request, RoutingNetwork network, TransactionContext transaction)
    {
        return DeferredResult.SUCCESS;
    }

    default long store(ItemVariant variant, long maxAmount, RoutingNetworkImpl.StoreRouter router, TransactionContext transaction) { return 0; }

    /// @return A direction that the pipe will accept crafting ingredients from. Null if no valid face.
    @Nullable NodePos requestDestPosDir();

    default void notifyCraft(ResourceAmount<ItemVariant> result, List<Dependency> dependencies, long batches) { }

    record Dependency(Predicate<ItemVariant> predicate, long amount) {}

    enum DeferredResult
    {
        SUCCESS,
        FAIL,
        PASS
    }

    /// Represents the operation that fulfils a request from a RoutablePipe.
    /// Should be returned by request so that the network controller can optionally cancel the operation depending on BOM status.
    @FunctionalInterface
    interface RoutingOperation
    {
        long apply(TransactionContext transaction);
    }

    record Result(RoutingOperation operation, long amount, long deferredAmount, ItemVariant deferredResult)
    {
        public static final Result FAIL = new Result(
                t -> 0, 0, 0, ItemVariant.of(ItemStack.EMPTY));

        public static Result immediate(RoutingOperation operation, long amount)
        {
            return new Result(operation, amount, 0, ItemVariant.of(ItemStack.EMPTY));
        }

        public long total()
        {
            return amount + deferredAmount;
        }
    }

    record IngredientResult(long amount, long deferredAmount, ItemVariant deferredResult, Collection<RecipeMatching.Residual> residuals)
    {
        public static final IngredientResult FAIL = new IngredientResult(0, 0, ItemVariant.of(ItemStack.EMPTY), List.of());

        public static IngredientResult immediate(long amount)
        {
            return new IngredientResult(amount, 0, ItemVariant.of(ItemStack.EMPTY), List.of());
        }

        public long total()
        {
            return amount + deferredAmount;
        }
    }
}
