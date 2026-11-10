package com.neep.neepmeat.transport.api.item_network;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Craftable
{
    ResourceAmount<ItemVariant> result();

    /// Calculates the dependencies required to produce the given amount of this craftable.
    /// @return null if the operation is invalid.
    @Nullable
    Dependencies getDependencies(long amount, TransactionContext transaction);

    RoutablePipe underlying();

    /// A view of a Craftable's dependencies that accounts for already-present resources.
    /// Example: A fabricator for making crafting tables is kept stocked with wood by a separate system.
    record Dependencies(List<RoutablePipe.Dependency> dependencies, long extra, int batches) { }

    /// Keeps track of the state of all available resources during BOM generation.
    class ProvisionContext
    {
        private final List<View> allItems;
        private final List<Craftable> craftables;

        public ProvisionContext(Stream<View> storageViews, List<Craftable> craftables)
        {
            allItems = storageViews.collect(ObjectArrayList::new, ObjectArrayList::add, ObjectArrayList::addAll);
            this.craftables = craftables;
        }

        public Stream<View> getReal(Predicate<ItemVariant> predicate)
        {
            return allItems.stream().filter(v -> predicate.test(v.resource));
            // TODO: this can be optimised by changing trivial implementations of Predicate to a special thingy
        }

        /// @return Craftables satisfying the resource or null if nothing can craft the required item.
        public Craftable getCraftable(Predicate<ItemVariant> predicate, long amount)
        {
            for (var view : craftables)
            {
                if (predicate.test(view.result().resource()))
                {
                    return view;
                }
            }

            // Fail
            return null;
        }

        /// Simulates buffering of excess outputs.
        public void insert(RoutablePipe underlying, ItemVariant resource, long amount)
        {
            if (amount > 0 && !resource.isBlank())
            {
                // TODO: deduplicate
                allItems.add(new View(resource, amount, underlying, true));
            }
        }

        /// Represents all available (real) items of this type in the network.
        public static class View
        {
            private final ItemVariant resource;
            public final long initialAmount;
            long amount;
            private final RoutablePipe underlying;

            // Whether this view may not exist at the beginning of the crafting cycle.
            // If true, the underlying resource should be accessed through a deferred request.
            public final boolean future;

            public View(ItemVariant resource, long amount, RoutablePipe underlying, boolean future)
            {
                this.resource = resource;
                this.amount = amount;
                this.initialAmount = amount;
                this.underlying = underlying;
                this.future = future;
            }

            public long extract(long maxAmount)
            {
                long toExtract = Math.min(maxAmount, amount);
                if (toExtract > 0)
                {
                    amount -= toExtract;
                    return toExtract;
                }

                return 0;
            }

            public RoutablePipe underlying()
            {
                return underlying;
            }

            public ItemVariant resource()
            {
                return resource;
            }

            @Override
            public String toString()
            {
                return "View[%s, %s]".formatted(resource, amount);
            }

            public void insert(long amount)
            {
                this.amount += amount;
            }
        }
    }
}
