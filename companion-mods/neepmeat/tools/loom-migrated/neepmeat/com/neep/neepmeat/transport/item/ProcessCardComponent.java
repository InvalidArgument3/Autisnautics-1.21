package com.neep.neepmeat.transport.item;

import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ProcessCardComponent extends ItemComponent
{
    public static final int SIZE = 3 * 5;
    private boolean dirty = true;
    private final ImplementedInventory inventory = ImplementedInventory.ofSize(SIZE, this::writeInventory);

    public ProcessCardComponent(ItemStack stack)
    {
        super(stack);
    }

    public ImplementedInventory getItems()
    {
        if (dirty)
        {
            inventory.readNbt(getCompound("dependencies"));
            dirty = false;
        }

        return inventory;
    }
    
    public CompoundTag getItemsNbt()
    {
        return getCompound("dependencies");
    }

    private void writeInventory()
    {
        putCompound("dependencies", inventory.writeNbt(new CompoundTag()));
    }

    @Override
    public void onTagInvalidated()
    {
        super.onTagInvalidated();
        dirty = true;
    }

    public void setItemsNbt(CompoundTag nbt)
    {
        putCompound("dependencies", nbt);
        dirty = true;
    }

    public void setResult(ResourceAmount<ItemVariant> result)
    {
        putCompound("result", MeatlibStorageUtil.itemAmountToNbt(result));
        dirty = true;
    }

    public ResourceAmount<ItemVariant> getResult()
    {
        return MeatlibStorageUtil.amountFromNbt(getCompound("result"));
    }

    public ItemStack getResultStack()
    {
        var r = getResult();
        return r.resource().toStack((int) r.amount());
    }
}
