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

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.network.ChannelManager;
import com.neep.meatlib.screen.ScreenReadyListener;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.machine.content_detector.InventoryDetector;
import com.neep.neepmeat.machine.content_detector.InventoryDetectorBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContentDetectorScreenHandler extends BasicScreenHandler implements ScreenReadyListener
{
    private boolean ready;

    public final ChannelManager<UpdateButtons> updateButtons;
    public final ChannelManager<ButtonPress> buttonPress;
    private final InventoryDetector parent;

    public ContentDetectorScreenHandler(int syncId, Inventory playerInventory)
    {
        this(syncId, playerInventory, new SimpleContainer(9), InventoryDetector.EMPTY);
    }

    public ContentDetectorScreenHandler(int syncId, Inventory playerInventory, Container inventory, InventoryDetector detector)
    {
        super(NMScreenHandlers.CONTENT_DETECTOR_SCREEN_HANDLER, playerInventory, inventory, syncId, null);

        this.updateButtons = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "update_buttons"),
                ChannelFormat.builder(UpdateButtons.class).param(InventoryDetector.CountMode.CODEC).param(InventoryDetector.BehaviourMode.CODEC).build(),
                playerInventory.player);

        this.buttonPress = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "button_press"),
                ChannelFormat.builder(ButtonPress.class).param(ParamCodec.INT).build(),
                playerInventory.player);
        this.parent = detector;

        checkContainerSize(inventory, 9);

        int m;
        int l;

        createSlotBlock(0, 0, 3, 3, inventory, 0, Slot::new);
        createPlayerSlots(0, 0, playerInventory);

//        //Our inventory
//        for (m = 0; m < 3; ++m)
//        {
//            for (l = 0; l < 3; ++l)
//            {
//                this.addSlot(new Slot(inventory, l + m * 3, 62 + l * 18, 17 + m * 18));
//            }
//        }
//        //The player inventory
//        for (m = 0; m < 3; ++m)
//        {
//            for (l = 0; l < 9; ++l)
//            {
//                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
//            }
//        }
//        //The player Hotbar
//        for (m = 0; m < 9; ++m)
//        {
//            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
//        }

        if (!isClient())
        {
            buttonPress.receiver(this::onButtonPress);
        }
    }

    private void onButtonPress(int button)
    {
        if (button == 0)
        {
            parent.cycleCountMode();
        }
        else if (button == 1)
        {
            parent.cycleBehaviourMode();
        }
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();
        if (ready)
        {
            updateButtons.emitter().update(parent.getCountMode(), parent.getBehaviourMode());
        }
    }

    @Override
    public boolean stillValid(Player player)
    {
        return this.inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot)
    {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem())
        {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.getContainerSize())
            {
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.moveItemStackTo(originalStack, 0, this.inventory.getContainerSize(), false))
            {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty())
            {
                slot.setByPlayer(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }
        }

        return newStack;
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        updateButtons.close();
        buttonPress.close();
    }

    @Override
    public void onScreenReady()
    {
        ready = true;
    }

    public interface UpdateButtons
    {
        void update(InventoryDetectorBlockEntity.CountMode countMode, InventoryDetectorBlockEntity.BehaviourMode behaviourMode);
    }

    public interface ButtonPress
    {
        void press(int button);
    }
}
