/*
 * Copyright (c) MeatWheeze 2026.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.transport.block.item_transport.entity;
import com.neep.neepmeat.machine.transport.ConnectedPipeBlock;
import com.neep.neepmeat.machine.transport.DeferredRequest;
import com.neep.neepmeat.machine.transport.FilterEntry;
import com.neep.neepmeat.machine.transport.FilterPipeMenu;
import com.neep.neepmeat.machine.transport.NetworkCrafterBlockEntity;
import com.neep.neepmeat.machine.transport.StorageBusBlock;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.PumpBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import com.neep.neepmeat.transport.block.fluid_transport.machine.PumpBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.FilterItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.MergePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.PipeDriverBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;

import com.neep.meatlib.inventory.ImplementedInventory;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

public class PersistentCraftingInventory implements ImplementedInventory, CraftingContainer
{
    private final Runnable markDirty;

    private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

    public PersistentCraftingInventory(Runnable markDirty)
    {
        this.markDirty = markDirty;
    }

    @Override
    public NonNullList<ItemStack> getItems()
    {
        return items;
    }

    @Override
    public void setChanged()
    {
        markDirty.run();
    }

    @Override
    public int getWidth()
    {
        return 3;
    }

    @Override
    public int getHeight()
    {
        return 3;
    }

    @Override
    public List<ItemStack> getItems()
    {
        return items;
    }

    @Override
    public void fillStackedContents(StackedContents finder)
    {
        for (ItemStack itemStack : this.items)
        {
            finder.accountSimpleStack(itemStack);
        }
    }

    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }

    public record Delegate(CraftingContainer delegate, AbstractContainerMenu handler) implements CraftingContainer
    {

        @Override
        public int getWidth()
        {
            return delegate.getWidth();
        }

        @Override
        public int getHeight()
        {
            return delegate.getHeight();
        }

        @Override
        public List<ItemStack> getItems()
        {
            return delegate.getItems();
        }

        @Override
        public int getContainerSize()
        {
            return delegate.getContainerSize();
        }

        @Override
        public boolean isEmpty()
        {
            return delegate.isEmpty();
        }

        @Override
        public ItemStack getItem(int slot)
        {
            return delegate.getItem(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount)
        {
            var result = delegate.removeItem(slot, amount);
            this.handler.slotsChanged(this);
            return result;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot)
        {
            var result = delegate.removeItemNoUpdate(slot);
            this.handler.slotsChanged(this);
            return result;
        }

        @Override
        public void setItem(int slot, ItemStack stack)
        {
            delegate.setItem(slot, stack);
            this.handler.slotsChanged(this);
        }

        @Override
        public void setChanged()
        {
            delegate.setChanged();
        }

        @Override
        public boolean stillValid(Player player)
        {
            return delegate.stillValid(player);
        }

        @Override
        public void fillStackedContents(StackedContents finder)
        {
            delegate.fillStackedContents(finder);
        }

        @Override
        public void clearContent()
        {
            delegate.clearContent();
        }
    }

    public static class Simple extends SimpleContainer implements CraftingContainer
    {
        public Simple(int size)
        {
            super(size);
        }

        @Override
        public int getWidth()
        {
            return 3;
        }

        @Override
        public int getHeight()
        {
            return 3;
        }

        @Override
        public List<ItemStack> getItems()
        {
            return items;
        }
    }
}
