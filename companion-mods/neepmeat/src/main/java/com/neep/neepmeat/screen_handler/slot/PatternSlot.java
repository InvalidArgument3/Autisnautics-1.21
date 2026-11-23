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

package com.neep.neepmeat.screen_handler.slot;

import java.util.Optional;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PatternSlot extends Slot
{
    protected final int maxAmount = 1;

    public PatternSlot(Container inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
    }

    public void onQuickCraft(ItemStack newItem, ItemStack original)
    {

    }

    @Override
    public int getMaxStackSize()
    {
        return 0;
    }

    public ItemStack safeTake(int min, int max, Player player)
    {
        return ItemStack.EMPTY;
    }

    public Optional<ItemStack> tryRemove(int min, int max, Player player)
    {
        return super.tryRemove(min, max, player);
    }

    @Override
    public void setByPlayer(ItemStack stack)
    {
        super.setByPlayer(stack);
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int amount)
    {
        ItemStack thisStack = stack.copy();
        thisStack.setCount(1);
        this.setByPlayer(thisStack);
        return stack;
    }

    @Override
    public ItemStack remove(int amount)
    {
        this.container.removeItemNoUpdate(this.getContainerSlot());
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return true;
    }

    @Override
    public boolean mayPickup(Player playerEntity)
    {
//        this.inventory.removeStack(this.getIndex());
        return true;
    }
}
