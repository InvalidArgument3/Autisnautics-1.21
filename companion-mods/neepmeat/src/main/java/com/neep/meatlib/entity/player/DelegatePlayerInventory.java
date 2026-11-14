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

package com.neep.meatlib.entity.player;

import java.util.Iterator;
import java.util.function.Predicate;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

// This was a bit of a failure. Don't use this!
public class DelegatePlayerInventory extends FakePlayerEntity.FakePlayerInventory
{
    private final Container inventory;
    private final Runnable markDirty;

    public DelegatePlayerInventory(Player player, Container inventory, Runnable markDirty)
    {
        super(player);
        this.inventory = inventory;
        this.markDirty = markDirty;
    }

    public ItemStack getSelected()
    {
        int sanitised = sanitiseSlot(selected);
        return inventory.getItem(sanitised);
    }

    private boolean hasRemainingSpaceForItem(ItemStack existingStack, ItemStack stack)
    {
        return !existingStack.isEmpty() && ItemStack.isSameItemSameComponents(existingStack, stack) && existingStack.isStackable() && existingStack.getCount() < existingStack.getMaxStackSize() && existingStack.getCount() < this.getMaxStackSize();
    }

    public int getFreeSlot()
    {
        for (int i = 0; i < inventory.getContainerSize(); ++i)
        {
            if ((inventory.getItem(i)).isEmpty())
            {
                return i;
            }
        }

        return -1;
    }

    public void setPickedItem(ItemStack stack)
    {
    }

    public void pickSlot(int slot)
    {
    }

    public int findSlotMatchingItem(ItemStack stack)
    {
        for (int i = 0; i < inventory.getContainerSize(); ++i)
        {
            if (!(inventory.getItem(i)).isEmpty() && ItemStack.isSameItemSameComponents(stack, inventory.getItem(i)))
            {
                return i;
            }
        }

        return -1;
    }

    public int findSlotMatchingUnusedItem(ItemStack stack)
    {
        for (int i = 0; i < inventory.getContainerSize(); ++i)
        {
            ItemStack itemStack = inventory.getItem(i);
            if (!inventory.getItem(i).isEmpty() && ItemStack.isSameItemSameComponents(stack, inventory.getItem(i)) && !inventory.getItem(i).isDamaged() && !itemStack.isEnchanted() && !itemStack.hasCustomHoverName())
            {
                return i;
            }
        }

        return -1;
    }

    public int getSuitableHotbarSlot()
    {
        int i;
        int j;
        for (i = 0; i < 9; ++i)
        {
            j = (this.selected + i) % 9;
            if (inventory.getItem(j).isEmpty())
            {
                return j;
            }
        }

        for (i = 0; i < 9; ++i)
        {
            j = (this.selected + i) % 9;
            if (!inventory.getItem(j).isEnchanted())
            {
                return j;
            }
        }

        return this.selected;
    }

    public void swapPaint(double scrollAmount)
    {

    }

    public int clearOrCountMatchingItems(Predicate<ItemStack> shouldRemove, int maxCount, Container craftingInventory)
    {
        int i = 0;
        boolean bl = maxCount == 0;
        i += ContainerHelper.clearOrCountMatchingItems(this, shouldRemove, maxCount - i, bl);
        i += ContainerHelper.clearOrCountMatchingItems(craftingInventory, shouldRemove, maxCount - i, bl);
        ItemStack itemStack = this.player.containerMenu.getCarried();
        i += ContainerHelper.clearOrCountMatchingItems(itemStack, shouldRemove, maxCount - i, bl);
        if (itemStack.isEmpty())
        {
            this.player.containerMenu.setCarried(ItemStack.EMPTY);
        }

        return i;
    }

    private int addResource(ItemStack stack)
    {
        int i = this.getSlotWithRemainingSpace(stack);
        if (i == -1)
        {
            i = this.getFreeSlot();
        }

        return i == -1 ? stack.getCount() : this.addResource(i, stack);
    }

    private int addResource(int slot, ItemStack stack)
    {
        Item item = stack.getItem();
        int i = stack.getCount();
        ItemStack itemStack = this.getItem(slot);
        if (itemStack.isEmpty())
        {
            itemStack = new ItemStack(item, 0);
            if (stack.hasTag())
            {
                com.neep.meatlib.util.MiscUtil.setItemTag(itemStack, com.neep.meatlib.util.MiscUtil.itemTag(stack).copy());
            }

            this.setItem(slot, itemStack);
        }

        int j = i;
        if (j > itemStack.getMaxStackSize() - itemStack.getCount())
        {
            j = itemStack.getMaxStackSize() - itemStack.getCount();
        }

        if (j > this.getMaxStackSize() - itemStack.getCount())
        {
            j = this.getMaxStackSize() - itemStack.getCount();
        }

        if (j == 0)
        {
            return i;
        }
        else
        {
            i -= j;
            itemStack.grow(j);
            itemStack.setPopTime(5);
            return i;
        }
    }

    public int getSlotWithRemainingSpace(ItemStack stack)
    {
        if (this.hasRemainingSpaceForItem(this.getItem(this.selected), stack))
        {
            return this.selected;
        }
        else if (this.hasRemainingSpaceForItem(this.getItem(40), stack))
        {
            return 40;
        }
        else
        {
            for (int i = 0; i < inventory.getContainerSize(); ++i)
            {
                if (this.hasRemainingSpaceForItem(inventory.getItem(i), stack))
                {
                    return i;
                }
            }

            return -1;
        }
    }

    public void tick()
    {
//        Iterator var1 = this.combinedInventory.iterator();
//
//        while (var1.hasNext())
//        {
//            DefaultedList<ItemStack> defaultedList = (DefaultedList) var1.next();
//
//            for (int i = 0; i < defaultedList.size(); ++i)
//            {
//                if (!((ItemStack) defaultedList.get(i)).isEmpty())
//                {
//                    ((ItemStack) defaultedList.get(i)).inventoryTick(this.player.getWorld(), this.player, i, this.selectedSlot == i);
//                }
//            }
//        }
    }

    public boolean add(ItemStack stack)
    {
        return this.add(-1, stack);
    }

    public boolean add(int slot, ItemStack stack)
    {
        if (stack.isEmpty())
            return false;

        try
        {
            if (stack.isDamaged())
            {
                if (slot == -1)
                {
                    slot = this.getFreeSlot();
                }

                if (slot >= 0)
                {
                    inventory.setItem(slot, stack.copyAndClear());
                    return true;
                }
                else if (this.player.getAbilities().instabuild)
                {
                    stack.setCount(0);
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                int i;
                do
                {
                    i = stack.getCount();
                    if (slot == -1)
                    {
                        stack.setCount(addResource(stack));
                    }
                    else
                    {
                        stack.setCount(addResource(slot, stack));
                    }
                }
                while (!stack.isEmpty() && stack.getCount() < i);

                return stack.getCount() < i;
            }
        }
        catch (Throwable var6)
        {
            Throwable throwable = var6;
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Adding item to inventory");
            CrashReportCategory crashReportSection = crashReport.addCategory("Item being added");
            crashReportSection.setDetail("Item ID", Item.getId(stack.getItem()));
            crashReportSection.setDetail("Item data", stack.getDamageValue());
            crashReportSection.setDetail("Item name", () ->
                    stack.getHoverName().getString());
            throw new ReportedException(crashReport);
        }
    }

    public void placeItemBackInInventory(ItemStack stack)
    {
        this.placeItemBackInInventory(stack, true);
    }

    public void placeItemBackInInventory(ItemStack stack, boolean notifiesClient)
    {
        while (true)
        {
            if (!stack.isEmpty())
            {
                int i = this.getSlotWithRemainingSpace(stack);
                if (i == -1)
                {
                    i = this.getFreeSlot();
                }

                if (i != -1)
                {
                    continue;
                }

                this.player.drop(stack, false);
            }

            return;
        }
    }

    public ItemStack removeItem(int slot, int amount)
    {
        slot = sanitiseSlot(slot);
        ItemStack prev = getItem(slot);

        return !prev.isEmpty() ? prev.split(amount) : ItemStack.EMPTY;
    }

    public void removeItem(ItemStack stack)
    {
        for (int i = 0; i < inventory.getContainerSize(); ++i)
        {
            if (inventory.getItem(i) == stack)
            {
                inventory.setItem(i, ItemStack.EMPTY);
                break;
            }
        }
    }

    public ItemStack removeItemNoUpdate(int slot)
    {
        slot = sanitiseSlot(slot);
        ItemStack prev = getItem(slot);
        setItem(slot, ItemStack.EMPTY);
        return prev;
    }

    public void setItem(int slot, ItemStack stack)
    {
        inventory.setItem(sanitiseSlot(slot), stack);
    }

    public float getDestroySpeed(BlockState block)
    {
        return inventory.getItem(this.selected).getDestroySpeed(block);
    }

    public ListTag save(ListTag nbtList)
    {
        return nbtList;
    }

    public void load(ListTag nbtList)
    {
    }

    public int getContainerSize()
    {
        return inventory.getContainerSize();
    }

    public boolean isEmpty()
    {
        return inventory.isEmpty();
    }

    public ItemStack getItem(int slot)
    {
        return inventory.getItem(sanitiseSlot(slot));
    }

    public Component getName()
    {
        return Component.translatable("container.inventory");
    }

    public ItemStack getArmor(int slot)
    {
        return ItemStack.EMPTY;
    }

    public void hurtArmor(DamageSource damageSource, float amount, int[] slots)
    {
    }

    public void dropAll()
    {
        for (int i = 0; i < inventory.getContainerSize(); ++i)
        {
            ItemStack itemStack = inventory.getItem(i);
            if (!itemStack.isEmpty())
            {
                this.player.drop(itemStack, true, false);
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    public void setChanged()
    {
        markDirty.run();
    }

    public int getTimesChanged()
    {
        return 0;
    }

    public boolean stillValid(Player player)
    {
        if (this.player.isRemoved())
        {
            return false;
        }
        else
        {
            return !(player.distanceToSqr(this.player) > 64.0);
        }
    }

    public boolean contains(ItemStack stack)
    {
        var var2 = iterator(inventory);

        while (var2.hasNext())
        {
            ItemStack itemStack = var2.next();
            if (!itemStack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, stack))
            {
                return true;
            }
        }

        return false;
    }

    public boolean contains(TagKey<Item> tag)
    {
        var var2 = iterator(inventory);

        while (var2.hasNext())
        {
            ItemStack itemStack = var2.next();
            if (!itemStack.isEmpty() && itemStack.is(tag))
            {
                return true;
            }
        }

        return false;
    }

    public void replaceWith(Inventory other)
    {
    }

    public void clearContent()
    {
        for (int i = 0; i < inventory.getContainerSize(); ++i)
        {
            inventory.setItem(i, ItemStack.EMPTY);
        }
    }

    public void fillStackedContents(StackedContents finder)
    {

    }

    public ItemStack removeFromSelected(boolean entireStack)
    {
        return ItemStack.EMPTY;
//        ItemStack itemStack = this.getMainHandStack();
//        if (itemStack.isEmpty())
//        {
//            return ItemStack.EMPTY;
//        }
//        else
//        {
//            mainHandItem.decrement(entireStack ? itemStack.getCount() : 1);
//        }
//
//        return itemStack.isEmpty() ? ItemStack.EMPTY : this.removeStack(this.selectedSlot, entireStack ? itemStack.getCount() : 1);
    }

    private int sanitiseSlot(int slot)
    {
        return Mth.clamp(slot, 0, inventory.getContainerSize() - 1);
    }

    public SlotAccess getStackReference(int mappedIndex)
    {
        if (mappedIndex < 0 || mappedIndex >= inventory.getContainerSize())
            return SlotAccess.NULL;

        return SlotAccess.forContainer(inventory, mappedIndex);
    }

    public void equipStack(EquipmentSlot slot, ItemStack stack)
    {
        selected = sanitiseSlot(selected);

        if (slot == EquipmentSlot.MAINHAND)
        {
            int selected = findSlotMatchingItem(stack);
            if (selected != -1)
                selected = selected;
            else
                setItem(selected, stack);
        }
    }

    @Override
    public void setSelectedSlot(int slot)
    {
        slot = sanitiseSlot(slot);
        selected = slot;
    }

    public static Iterator<ItemStack> iterator(Container inventory)
    {
        return new Iterator<>()
        {
            int idx = 0;

            @Override
            public boolean hasNext()
            {
                return idx < inventory.getContainerSize();
            }

            @Override
            public ItemStack next()
            {
                return inventory.getItem(idx++);
            }
        };
    }
}
