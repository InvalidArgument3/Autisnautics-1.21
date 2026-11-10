package com.neep.neepmeat.transport.util;

import com.neep.neepmeat.transport.api.item_network.RoutablePipe;
import com.neep.neepmeat.transport.fluid_network.node.NodePos;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;

import java.util.List;

public class BOM
{
    private final List<Operation> operations = new ObjectArrayList<>();
    private final List<Operation> deferredOperations = new ObjectArrayList<>();
    private final List<CraftEntry> crafts = new ObjectArrayList<>();

    public void addOperation(RoutablePipe source, NodePos dest, ItemVariant predicate, long amount, boolean defer)
    {
        if (!defer)
            operations.add(new Operation(source, dest, predicate, amount));
        else
            deferredOperations.add(new Operation(source, dest, predicate, amount));
    }

    public void addCraftNotification(RoutablePipe source, List<RoutablePipe.Dependency> dependencies, ResourceAmount<ItemVariant> result, int batches)
    {
        crafts.add(new CraftEntry(source, dependencies, result, batches));
    }

    public List<Operation> operations()
    {
        return operations;
    }

    public List<Operation> deferredOperations()
    {
        return deferredOperations;
    }

    public List<CraftEntry> crafts()
    {
        return crafts;
    }

    public record Operation(RoutablePipe source, NodePos dest, ItemVariant resource, long amount) {}

    public record CraftEntry(RoutablePipe source, List<RoutablePipe.Dependency> dependencies, ResourceAmount<ItemVariant> result, int batches) {}
}
