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

package com.neep.meatlib.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageUtil;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Some fluid functions that are as similar as possible to StorageUtil versions but with added predicate parameters.
 */
@SuppressWarnings("UnstableApiUsage")
public class MeatlibStorageUtil
{
    public static final Codec<ItemVariant> ITEM_VARIANT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ItemVariant::getItem),
                    Codec.optionalField("nbt", CompoundTag.CODEC).<ItemVariant>forGetter(v -> Optional.ofNullable(v.getNbt()))
            ).apply(instance, (item, opt) -> ItemVariant.of(item, opt.orElse(null))));

    /**
     * For dealing with storages that expose all internal slots but don't indicate which ones can't be extracted from (Modern Industrialization).
     * @param predicate Executed for each non-empty resource. If the result is positive, extraction will be attempted.
     */
    @Nullable
    public static <T> ResourceAmount<T> findAndExtractContent(Storage<T> storage, long maxAmount, TransactionContext transaction, ResourcePredicate<T> predicate)
    {
        for (var view :  storage)
        {
            T resource = view.getResource();
            if (!view.isResourceBlank())
            {
                long insertable = predicate.tryTest(resource, Math.min(maxAmount, view.getAmount()));

                if (insertable <= 0)
                    continue;

                try (Transaction inner = transaction.openNested())
                {
                    long extracted = view.extract(resource, insertable, inner);

                    if (extracted > 0 && extracted <= insertable)
                    {
                        inner.commit();
                        return new ResourceAmount<>(resource, extracted);
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    public static <T> T findStoredResourceForInsertion(@Nullable Storage<T> storage, Storage<T> insertTo, T blank)
    {
        if (storage == null)
            return null;

        Set<T> things = new HashSet<>();
        things.add(blank);

        for (var view : insertTo)
        {
            T resource = view.getResource();

            // Blank slot means anything can be inserted (probably)
            if (resource.equals(blank))
                return StorageUtil.findStoredResource(storage);

            things.add(resource);
        }

        for (StorageView<T> view : storage.nonEmptyViews())
        {
            if (things.contains(view.getResource()))
            {
                return view.getResource();
            }
        }

        return null;
    }

    /**
     * The same as FAPI except the predicate has access to a TransactionContext.
     *
     * @param storage Why is this nullable?
     */
    @Nullable
    public static <T> ResourceAmount<T> findExtractableContent(@Nullable Storage<T> storage, BiPredicate<TransactionContext, T> filter, @Nullable TransactionContext transaction)
    {
        T extractableResource = findExtractableResource(storage, (t, r, l) -> filter.test(t, r), transaction);

        if (extractableResource != null)
        {
            long extractableAmount = MeatlibStorageUtil.simulateExtract(storage, extractableResource, Long.MAX_VALUE, transaction);

            if (extractableAmount > 0)
            {
                return new ResourceAmount<>(extractableResource, extractableAmount);
            }
        }

        return null;
    }

    // Storage::simulateExtract will be deprecated in 1.19.4. I should be able to use fnd and replace to correct this.
    public static <T> long simulateExtract(Storage<T> storage, T resource, long maxAmount, TransactionContext transaction)
    {
        return StorageUtil.simulateExtract(storage, resource, maxAmount, transaction); // 1.20
//        return storage.simulateExtract(resource, maxAmount, transaction); // 1.19
    }

    public static <T> long simulateInsert(Storage<T> storage, T resource, long amount, TransactionContext transaction)
    {
        return StorageUtil.simulateInsert(storage, resource, amount, transaction); // 1.20
//        return storage.simulateInsert(resource, amount, transaction); // 1.19
    }

//    @Nullable
//    public static <T> T findExtractableResource(@Nullable Storage<T> storage, BiPredicate<TransactionContext, T> filter, @Nullable TransactionContext transaction)
//    {
//        if (storage == null) return null;
//
//        try (Transaction nested = Transaction.openNested(transaction))
//        {
//            for (StorageView<T> view : storage.iterable(nested))
//            {
//                // Extract below could change the resource, so we have to query it before extracting.
//                T resource = view.getResource();
//
//                // Provide the predicate with the transaction
//                if (!view.isResourceBlank() && filter.test(nested, resource) && view.extract(resource, Long.MAX_VALUE, nested) > 0)
//                {
//                    // Will abort the extraction.
//                    return resource;
//                }
//            }
//        }
//        return null;
//    }

    @Nullable
    public static <T> T findExtractableResource(@Nullable Storage<T> storage, TriFunction<TransactionContext, T, Long, Boolean> filter, @Nullable TransactionContext transaction)
    {
        if (storage == null)
            return null;

        try (Transaction nested = Transaction.openNested(transaction))
        {
            for (StorageView<T> view : storage)
            {
                // Extract below could change the resource, so we have to query it before extracting.
                T resource = view.getResource();

                if (!view.isResourceBlank() && filter.apply(nested, resource, view.getAmount()) && view.extract(resource, Long.MAX_VALUE, nested) > 0)
                {
                    // Will abort the extraction.
                    return resource;
                }
            }
        }
        return null;
    }

    public static void scatterAmount(Level world, BlockPos pos, @Nullable ResourceAmount<ItemVariant> resourceAmount)
    {
        if (resourceAmount != null)
        {
            Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, resourceAmount.resource().toStack((int) resourceAmount.amount()));
        }
    }

    public static void scatterAmount(Level world, BlockPos pos, ItemVariant variant, long amount)
    {
        Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, variant.toStack((int) amount));
    }

    public static void scatter(Level world, BlockPos pos, @Nullable ItemStack stack)
    {
        if (stack != null)
        {
            Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        }
    }

    public static CompoundTag itemAmountToNbt(ResourceAmount<ItemVariant> resourceAmount)
    {
        CompoundTag nbt = resourceAmount.resource().toNbt();
        nbt.putLong("amount", resourceAmount.amount());
        return nbt;
    }

    public static CompoundTag fluidAmountToNbt(ResourceAmount<FluidVariant> resourceAmount)
    {
        CompoundTag nbt = resourceAmount.resource().toNbt();
        nbt.putLong("amount", resourceAmount.amount());
        return nbt;
    }

    public static ResourceAmount<ItemVariant> amountFromNbt(CompoundTag nbt)
    {
        ItemVariant variant = ItemVariant.fromNbt(nbt);
        return new ResourceAmount<>(variant, nbt.getLong("amount"));
    }

    public static ResourceAmount<FluidVariant> fluidAmountFromNbt(CompoundTag nbt)
    {
        FluidVariant variant = FluidVariant.fromNbt(nbt);
        return new ResourceAmount<>(variant, nbt.getLong("amount"));
    }

    public static void scatter(Level world, BlockPos pos, @Nullable Storage<ItemVariant> storage)
    {
        if (storage == null)
            return;

        try (Transaction transaction = Transaction.openOuter())
        {
            for (var view : storage.nonEmptyViews())
            {
                ItemVariant variant = view.getResource();
                long extracted = view.extract(view.getResource(), Long.MAX_VALUE, transaction);
                Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, variant.toStack((int) extracted));
            }
        }
    }

    public static void scatterNoTransaction(Level world, BlockPos pos, Storage<ItemVariant> storage)
    {
        for (StorageView<ItemVariant> view : storage)
        {
            Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, view.getResource().toStack((int) view.getAmount()));
        }
    }

    public static <T> ResourcePredicate<T> insertionPredicate(@Nullable Storage<T> storage)
    {
        if (storage == null)
            return (v, a) -> a;

        Set<T> found = new HashSet<>();
        for (var view : storage)
        {
            if (view.isResourceBlank())
            {
                return (v, a) -> a;
            }

            found.add(view.getResource());
        }

        return (r, a) -> found.contains(r) ? a : 0;
    }

    public static <T> Predicate<T> deepInsertionPredicate(@Nullable Storage<T> storage, long maxAmount, TransactionContext transaction)
    {
        if (storage == null)
            return v -> true;

        Set<T> found = new HashSet<>();
        for (var view : storage)
        {
            if (view.isResourceBlank())
            {
                return v -> true;
            }

            try (Transaction inner = transaction.openNested())
            {
                storage.insert(view.getResource(), maxAmount, inner);
            }

            found.add(view.getResource());
        }

        return found::contains;
    }

    public static ResourceAmount<ItemVariant> amountFromStack(ItemStack s)
    {
        return new ResourceAmount<>(ItemVariant.of(s), s.getCount());
    }

    public static void closeCallback(TransactionContext t, TransactionContext.Result r, Runnable success)
    {
        if (r.wasCommitted())
        {
            if (t.nestingDepth() > 0)
            {
                t.getOpenTransaction(t.nestingDepth() - 1).addCloseCallback((t1, r1) -> closeCallback(t1, r1, success));
            }
            else
            {
                t.addOuterCloseCallback(r1 ->
                {
                    if (!r1.wasCommitted())
                        return;

                    success.run();
                });
            }
        }
    }
}
