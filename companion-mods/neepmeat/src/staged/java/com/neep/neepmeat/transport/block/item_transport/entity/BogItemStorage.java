package com.neep.neepmeat.transport.block.item_transport.entity;

import com.neep.meatlib.util.NbtSerialisable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.SingleVariantStorage;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import java.util.Iterator;
import java.util.function.Consumer;

public abstract class BogItemStorage implements Storage<ItemVariant>, NbtSerialisable
{
    protected final ObjectArrayList<StackView> viewList = new ObjectArrayList<>();

    public BogItemStorage()
    {
    }

    protected void updateLists(Consumer<StorageView<ItemVariant>> discard)
    {
        int oldSize = viewList.size();
        int newSize = getStackCapacity();
        if (newSize == oldSize)
        {
            // Nothing to do. Yay.
        }
        if (newSize < oldSize)
        {
            for (int i = newSize; i < oldSize; i++)
            {
                var view = viewList.get(i);
                discard.accept(view);
            }

            viewList.size(newSize);
        }
        else
        {
            viewList.size(newSize);
            for (int i = 0; i < newSize; ++i)
            {
                if (viewList.get(i) == null)
                {
                    viewList.set(i, new StackView());
                }
            }
        }
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction)
    {
        long remaining = maxAmount;
        for (var view : viewList)
        {
            remaining -= view.insert(resource, remaining, transaction);

            if (remaining == 0)
                break;
        }

        return maxAmount - remaining;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction)
    {
        long remaining = maxAmount;
        for (var view : viewList)
        {
            if (view.getResource().equals(resource))
                remaining -= view.extract(resource, remaining, transaction);

            if (remaining == 0)
                break;
        }

        return maxAmount - remaining;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator()
    {
        // Aah, shut up
        // In kotlin this wouldn't be a problem
        return (Iterator<StorageView<ItemVariant>>) (Iterator<?>) viewList.iterator();
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        var list = new ListTag();
        for (StackView storageViews : viewList)
        {
            list.add(storageViews.writeToNbt(new CompoundTag()));
        }
        nbt.put("slots", list);
//        nbt.putInt("current_size", viewList.size());
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        var list = nbt.getList("slots", Tag.TAG_COMPOUND);

//        viewList.size(nbt.getInt("current_size"));
        viewList.clear();
        for (int i = 0; i < list.size(); i++)
        {
            var view = new StackView();
            view.readNbt(list.getCompound(i));
            viewList.add(view);
        }
    }

    protected abstract int getStackCapacity();

    protected abstract long getStackSize();

    protected class StackView extends SingleVariantStorage<ItemVariant>
    {
        @Override
        protected ItemVariant getBlankVariant()
        {
            return ItemVariant.BLANK;
        }

        @Override
        protected long getCapacity(ItemVariant variant)
        {
            return getStackSize();
        }

        public CompoundTag writeToNbt(CompoundTag nbt)
        {
            writeNbt(nbt);
            return nbt;
        }

        public void readNbt(CompoundTag nbt)
        {
            this.variant = ItemVariant.fromNbt(nbt.getCompound("variant"));
            this.amount = nbt.getLong("amount");
        }
    }
}
