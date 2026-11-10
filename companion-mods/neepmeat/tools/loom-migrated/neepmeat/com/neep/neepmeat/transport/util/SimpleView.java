package com.neep.neepmeat.transport.util;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public record SimpleView(ResourceAmount<ItemVariant> variant) implements StorageView<ItemVariant>
{
    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction)
    {
        return 0;
    }

    @Override
    public boolean isResourceBlank()
    {
        return false;
    }

    @Override
    public ItemVariant getResource()
    {
        return variant.resource();
    }

    @Override
    public long getAmount()
    {
        return variant.amount();
    }

    @Override
    public long getCapacity()
    {
        return variant.amount();
    }
}
