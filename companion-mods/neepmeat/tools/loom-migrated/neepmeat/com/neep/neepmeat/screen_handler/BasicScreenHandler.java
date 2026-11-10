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

package com.neep.neepmeat.screen_handler;

import com.google.common.collect.Streams;
import com.neep.meatlib.screen.Closable;
import com.neep.meatlib.screen.ScreenReadyListener;
import com.neep.meatlib.screen.WidgetHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class BasicScreenHandler extends AbstractContainerMenu implements ScreenReadyListener
{
    protected final Container inventory;
    protected Inventory playerInventory;
    @Nullable protected ContainerData propertyDelegate;

    private final List<WidgetHandler> widgetHandlers = new ObjectArrayList<>();
    private final List<Closable> closables = new ObjectArrayList<>();

    protected boolean ready;

    protected BasicScreenHandler(@Nullable MenuType<?> type, Inventory playerInventory, int syncId)
    {
        this(type, playerInventory, null, syncId, null);
    }

    protected BasicScreenHandler(@Nullable MenuType<?> type, Inventory playerInventory, @Nullable Container inventory, int syncId, @Nullable ContainerData delegate)
    {
        super(type, syncId);
        this.propertyDelegate = delegate;
        this.inventory = inventory == null ? new SimpleContainer(0) : inventory;
        this.playerInventory = playerInventory;

        if (inventory != null)
            inventory.startOpen(playerInventory.player);

        if (propertyDelegate != null)
            this.addDataSlots(delegate);
    }

    /**
     * Register a {@link WidgetHandler} to be automatically updated and closed at the appropriate time.
     */
    protected <T extends WidgetHandler> T addWidgetHandler(T handler)
    {
        widgetHandlers.add(handler);
        closables.add(handler);
        return handler;
    }

    protected <T extends Closable> T addClosable(T closable)
    {
        closables.add(closable);
        return closable;
    }

    public int getProperty(int i)
    {
        if (propertyDelegate != null) return propertyDelegate.get(i);
        return 0;
    }

    public boolean isClient()
    {
        return playerInventory.player.level().isClientSide();
    }

    public void setData(int i, int value)
    {
        if (propertyDelegate != null) propertyDelegate.set(i, value);
    }

    protected List<Slot> createPlayerSlots(int startX, int startY, Inventory playerInventory)
    {
        var list1 = createInventory(startX, startY, playerInventory);
        var list2 = createHotbar(startX, startY + 58, playerInventory);
        return Streams.concat(list1.stream(), list2.stream()).toList();
    }

    protected List<Slot> createInventory(int startX, int startY, Inventory playerInventory)
    {
        int prevIndex = slots.size();

        for (int m = 0; m < 3; ++m)
        {
            for (int l = 0; l < 9; ++l)
            {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, startX + l * 18, startY + m * 18));
            }
        }

        return slots.subList(prevIndex, prevIndex + 3 * 9);
    }

    protected List<Slot> createHotbar(int startX, int startY, Inventory playerInventory)
    {
        int prevIndex = slots.size();

        for (int m = 0; m < 9; ++m)
        {
            this.addSlot(new Slot(playerInventory, m, startX + m * 18, startY));
        }

        return slots.subList(prevIndex, prevIndex + 9);
    }

    protected List<Slot> createSlotBlock(int startX, int startY, int nx, int ny, Container inventory, int startIndex, SlotConstructor constructor)
    {
        int prevIndex = slots.size();

        int m, l;
        for (m = 0; m < ny; ++m)
        {
            for (l = 0; l < nx; ++l)
            {
                this.addSlot(constructor.construct(inventory, startIndex + l + m * nx, startX + l * 18, startY + m * 18));
            }
        }

        return slots.subList(prevIndex, prevIndex + nx * ny);
    }

    protected List<Slot> createSlotBlock(int n, Container inventory, int startIndex, SlotConstructor constructor)
    {
        int prevIndex = slots.size();

        int m;
        for (m = 0; m < n; ++m)
        {
            this.addSlot(constructor.construct(inventory, startIndex + m, 0, 0));
        }

        return slots.subList(prevIndex, prevIndex + n);
    }

    // For some reason, not implementing this causes the game to freeze when shift-clicking.
    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        ItemStack originalCopy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem())
        {
            ItemStack mutableStack = slot.getItem();
            originalCopy = mutableStack.copy();
            if (index < this.inventory.getContainerSize())
            {
                if (!this.moveItemStackTo(mutableStack, this.inventory.getContainerSize(), this.slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(mutableStack, originalCopy);
                slot.remove(64); // This 'fixes' a strange thing in WorkstationScreenHandler.
                slot.setChanged();
            }
            else if (!this.moveItemStackTo(mutableStack, 0, this.inventory.getContainerSize(), false))
            {
                return ItemStack.EMPTY;
            }

            if (mutableStack.isEmpty())
            {
                slot.setByPlayer(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }
        }
        return originalCopy;
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();

        if (ready)
        {
            for (WidgetHandler handler : widgetHandlers)
            {
                handler.sendUpdates();
            }
        }
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);

        closables.forEach(Closable::close);
    }

    /// Called by the screen when the client notifies it that it has been removed
    public void onRemoved()
    {
        // TODO: this doesn't work
//        if (MeatLib.isClient())
//        {
//            closables.forEach(Closable::close);
//        }
    }

    @FunctionalInterface
    public interface SlotConstructor
    {
        Slot construct(Container inventory, int index, int x, int y);
    }

    @Override
    public boolean stillValid(Player player)
    {
        return true;
//        return this.inventory.canPlayerUse(player);
    }

    @Override
    public void onScreenReady()
    {
        ready = true;
    }
}