package com.neep.neepmeat.screen_handler;

import com.neep.neepmeat.plc.PLCBlocks;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class CardSlot extends Slot
{
    public CardSlot(Container inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
    }

    @Override
    public int getMaxStackSize()
    {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack)
    {
        return 1;
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int count)
    {
        if (!stack.isEmpty() && this.mayPlace(stack))
        {
            ItemStack itemStack = this.getItem();
            int i = Math.min(Math.min(count, stack.getCount()), this.getMaxStackSize(stack) - itemStack.getCount());
            if (itemStack.isEmpty())
            {
                this.setByPlayer(stack.split(i));
            }
            else if (ItemStack.isSameItemSameComponents(itemStack, stack))
            {
                stack.shrink(i);
                itemStack.grow(i);
                this.setByPlayer(itemStack);
            }

            return stack;
        }
        else
        {
            return stack;
        }
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return super.mayPlace(stack) && !hasItem() && stack.is(validItem());
    }

    public abstract Item validItem();
}
