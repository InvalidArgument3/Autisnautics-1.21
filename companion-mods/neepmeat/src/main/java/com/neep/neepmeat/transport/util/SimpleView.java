package com.neep.neepmeat.transport.util;

import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.TransactionContext;

public record SimpleView(ResourceAmount<ItemVariant> variant) implements StorageView<ItemVariant>
{
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
