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

import com.google.common.collect.Streams;
import com.neep.meatlib.api.network.CompositeParamCodec;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.api.network.RecordParamCodec;
import com.neep.neepmeat.NeepMeat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface RoutingNetwork
{
    BlockApiLookup<RoutingNetwork, Void> LOOKUP = BlockApiLookup.get(new ResourceLocation(NeepMeat.NAMESPACE, "routing_network"), RoutingNetwork.class, Void.class);

    Stream<ResourceAmount<ItemVariant>> getAllAvailable(Predicate<ItemVariant> predicate, TransactionContext transaction);

    Stream<Craftable> getAllCraftable(Predicate<ItemVariant> predicate, TransactionContext transaction);

    default List<Entry> listAllAvailable(Predicate<ItemVariant> predicate, TransactionContext transaction)
    {
        return Streams.concat(
                getAllAvailable(predicate, transaction)
                        .map(Entry::new),
                getAllCraftable(predicate, transaction)
                    .map(Entry::new))
                .collect(Collectors.collectingAndThen(
                    Collectors.toMap(
                            Entry::resource, Function.identity(),
                            RoutingNetwork::combine), m -> new ObjectArrayList<>(m.values()))
                );
    }

    static Entry combine(Entry am1, Entry am2)
    {
        // Discard the craftable amount if a different entry has a real amount
        long amount;
        if (am1.craftable() && am2.craftable())
            amount = 0;
        else if (am1.craftable())
            amount = am2.amount();
        else if (am2.craftable())
            amount = am1.amount();
        else
            amount = am1.amount() + am2.amount();

        return new Entry(am1.resource(), amount,
                am1.craftable() || am2.craftable(),
                am1.real() || am2.real()
        );
    }

    /**
     * Transferring items into pipes requires outer transaction callbacks. To prevent duping, only commit the outer transaction if all nested operations were successful.
     */
    boolean route(Predicate<ItemVariant> predicate, long amount, BlockPos inPos, Direction inDir, BlockPos outPos, Direction outDir, RequestType type, TransactionContext transaction);

//    default boolean route(ResourceAmount<ItemVariant> stack, BlockPos inPos, Direction inDir, BlockPos outPos, Direction outDir, RequestType type, TransactionContext transaction)
//    {
//        return route()
//    }

    /**
     * Transferring items into pipes requires outer transaction callbacks. To prevent duping, only commit the outer transaction if all nested operations were successful.
     */
    default boolean request(ResourceAmount<ItemVariant> stack, BlockPos pos, Direction outDir, RequestType type, RequestContext context, TransactionContext transaction)
    {
        return request(v -> v.equals(stack.resource()), stack.amount(), pos, outDir, type, context, transaction);
    }

    boolean request(Predicate<ItemVariant> predicate, long amount, BlockPos pos, Direction outDir, RequestType type, RequestContext context, TransactionContext transaction);

    /// @param inPos Position to eject from.
    /// @param inFace Direction to eject from relative to inPos
    long store(ItemVariant variant, long amount, BlockPos inPos, Direction inFace, TransactionContext transaction);

    void invalidate();

    boolean needsUpdate();

    void update();

    long getVersion();

    boolean isValid();

    void serverTick();

    RoutingNetwork DEFAULT = new RoutingNetwork()
    {
        @Override
        public Stream<ResourceAmount<ItemVariant>> getAllAvailable(Predicate<ItemVariant> predicate, TransactionContext transaction) { return Stream.empty(); }

        @Override
        public Stream<Craftable> getAllCraftable(Predicate<ItemVariant> predicate, TransactionContext transaction)
        {
            return Stream.empty();
        }

        @Override
        public boolean route(Predicate<ItemVariant> predicate, long amount, BlockPos inPos, Direction inDir, BlockPos outPos, Direction outDir, RequestType type, TransactionContext transaction)
        {
            return false;
        }

        @Override
        public boolean request(Predicate<ItemVariant> predicate, long amount, BlockPos pos, Direction outDir, RequestType type, RequestContext context, TransactionContext transaction) { return false; }

        @Override
        public long store(ItemVariant variant, long amount, BlockPos inPos, Direction inFace, TransactionContext transaction)
        {
            return 0;
        }

        @Override
        public void invalidate() {}

        @Override
        public boolean needsUpdate() { return false; }

        @Override
        public void update() {}

        @Override
        public long getVersion() { return 0;}

        @Override
        public boolean isValid()
        {
            return true;
        }

        @Override
        public void serverTick()
        {

        }
    };

    /// A structure passed through each request to prevent infinite recursion.
    /// Based on the assumption that a recipe should never
    class RequestContext
    {
        public int recursionDepth;
//        Set<RoutablePipe> visitedPipes = new HashSet<>();
//
//        public boolean notVisited(RoutablePipe routable)
//        {
//            return !visitedPipes.contains(routable);
//        }
//
//        public void visit(
    }

    enum RequestType
    {
        ANY_AMOUNT(false),
        EXACT_AMOUNT(true);

        private final boolean matchExact;

        RequestType(boolean matchExact)
        {
            this.matchExact = matchExact;
        }

        public boolean satisfied(long requested, long found)
        {
            return requested == found && matchExact || found >= requested && !matchExact;
        }
    }

    // TODO: add timeout duration
    final class DeferredRequest
    {
        private final BlockPos sourcePos;
        public final BlockPos requesterPos;
        public final Direction requesterFace;
        private final ItemVariant itemVariant;

        private final long requested;

        @Nullable private RoutablePipe pipe;

        public static final ParamCodec<DeferredRequest> PARAM_CODEC = CompositeParamCodec.builder(DeferredRequest.class)
                .param(o -> o.sourcePos, ParamCodecs.BLOCK_POS)
                .param(o -> o.requesterPos, ParamCodecs.BLOCK_POS)
                .param(o -> o.requesterFace, ParamCodec.ofEnum(Direction.class))
                .param(o -> o.requested, ParamCodec.LONG)
                .param(o -> o.itemVariant, ParamCodecs.ITEM_VARIANT)
                .build();

        public DeferredRequest(BlockPos sourcePos, BlockPos requesterPos, Direction requesterFace, long requested, ItemVariant itemVariant)
        {
            this.sourcePos = sourcePos;
            this.requesterPos = requesterPos;
            this.requesterFace = requesterFace;
            this.itemVariant = itemVariant;
            this.requested = requested;
        }

        public long getRequested()
        {
            return requested;
        }

        public ItemVariant getItemVariant()
        {
            return itemVariant;
        }

        public RoutablePipe getPipe(Level world)
        {
            if (pipe == null)
            {
                if (world.getBlockEntity(sourcePos) instanceof RoutablePipe routablePipe)
                    this.pipe = routablePipe;
            }

            return pipe;
        }

        @Override
        public String toString()
        {
            return "Request{" +
                    "requested=" + requested +
                    ", itemVariant=" + itemVariant +
                    ", requesterPos=" + requesterPos +
                    ", sourcePos=" + sourcePos +
                    '}';
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof DeferredRequest that)) return false;
            return requested == that.requested && Objects.equals(sourcePos, that.sourcePos) && Objects.equals(requesterPos, that.requesterPos) && requesterFace == that.requesterFace && Objects.equals(itemVariant, that.itemVariant);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(sourcePos, requesterPos, requesterFace, itemVariant, requested);
        }

        public CompoundTag toNbt()
        {
            CompoundTag nbt = new CompoundTag();

            nbt.put("pos", NbtUtils.writeBlockPos(sourcePos));
            nbt.put("requester_pos", NbtUtils.writeBlockPos(requesterPos));
            nbt.putInt("requester_face", requesterFace.ordinal());
            nbt.put("item", itemVariant.toNbt());
            nbt.putLong("amount", requested);
            return nbt;
        }

        public static DeferredRequest fromNbt(CompoundTag nbt)
        {
            return new DeferredRequest(
                    NbtUtils.readBlockPos(nbt.getCompound("pos")),
                    NbtUtils.readBlockPos(nbt.getCompound("requester_pos")),
                    Direction.values()[nbt.getInt("requester_face")],
                    nbt.getLong("amount"),
                    ItemVariant.fromNbt(nbt.getCompound("item"))
                    );
        }
    }

    record Entry(ItemVariant resource, long amount, boolean craftable, boolean real)
    {
        public static final ParamCodec<Entry> PARAM_CODEC = RecordParamCodec.builder(Entry.class)
                .param(ParamCodecs.ITEM_VARIANT).param(ParamCodec.LONG).param(ParamCodec.BOOLEAN).param(ParamCodec.BOOLEAN).build();

        public Entry(Craftable craftable)
        {
            this(craftable.result().resource(), craftable.result().amount(), true, false);
        }

        public Entry(ResourceAmount<ItemVariant> ra)
        {
            this(ra.resource(), ra.amount(), false, true);
        }
    }
}
