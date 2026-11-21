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

package com.neep.neepmeat.client.screen.button;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

public class ResultSlot extends Slot
{
    private final Player player;
    private int amount; // I have no idea why I have done this, but it seems important.

    public ResultSlot(Player player, TransientCraftingContainer input, Container inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
        this.player = player;
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return false;
    }

    @Override
    public ItemStack getItem()
    {
        return super.getItem();
    }

    @Override
    public ItemStack remove(int amount)
    {
        if (this.hasItem())
        {
            this.amount += Math.min(amount, this.getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    public void onQuickCraft(ItemStack newItem, ItemStack original)
    {
        super.onQuickCraft(newItem, original);
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount)
    {
        this.amount += amount;
        this.checkTakeAchievements(stack);
    }

    @Override
    protected void onSwapCraft(int amount) {
        this.amount += amount;
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack)
    {
        if (this.amount > 0)
        {
            stack.onCraftedBy(this.player.level(), this.player, this.amount);
        }
        if (this.container instanceof RecipeHolder)
        {
//            ((RecipeUnlocker) this.inventory).unlockLastRecipe(this.player, );
        }
        this.amount = 0;
    }

    @Override
    public void onTake(Player player, ItemStack stack)
    {
        this.checkTakeAchievements(stack);
//        DefaultedList<ItemStack> defaultedList = player.world.getRecipeManager().getRemainingStacks(RecipeType.CRAFTING, this.input, player.world);
//        for (int i = 0; i < defaultedList.size(); ++i)
//        {
//            ItemStack itemStack = this.input.getStack(i);
//            ItemStack itemStack2 = defaultedList.get(i);
//            if (!itemStack.isEmpty())
//            {
//                this.input.removeStack(i, 1);
//                itemStack = this.input.getStack(i);
//            }
//            if (itemStack2.isEmpty()) continue;
//            if (itemStack.isEmpty())
//            {
//                this.input.setStack(i, itemStack2);
//                continue;
//            }
//            if (ItemStack.areItemsEqualIgnoreDamage(itemStack, itemStack2) && ItemStack.areNbtEqual(itemStack, itemStack2))
//            {
//                itemStack2.increment(itemStack.getCount());
//                this.input.setStack(i, itemStack2);
//                continue;
//            }
//            if (this.player.getInventory().insertStack(itemStack2)) continue;
//            this.player.dropItem(itemStack2, false);
//        }
    }
}
