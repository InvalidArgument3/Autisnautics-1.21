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
import com.neep.meatlib.network.ChannelManager;
import com.neep.neepbus.screen.NeepBusConfigScreenHandler;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.screen_handler.slot.PatternSlot;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FluidRationerScreenHandler extends BasicScreenHandler
{
    public static int PROP_TARGET_AMOUNT = 0;
    public static int PROPERTIES = 1;

    public final ChannelManager<Runnable> openConfigC2S;
    public final List<Slot> playerSlots;
    public final List<Slot> filterSlots;

    // Client
    public FluidRationerScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf)
    {
        this(syncId, playerInventory, new SimpleContainer(1), new SimpleContainerData(PROPERTIES), null);

        // Ensure the value is correct even before the PropertyDelegate syncs
        setData(PROP_TARGET_AMOUNT, buf.readVarInt());
    }

    // Server
    public FluidRationerScreenHandler(int syncId, Inventory playerInventory, Container inventory, @Nullable ContainerData delegate, NeepBusConfig config)
    {
        super(NMScreenHandlers.FLUID_RATIONER, playerInventory, inventory, syncId, delegate);

        this.openConfigC2S = ChannelManager.create(new ResourceLocation(NeepMeat.NAMESPACE, "mouth_open_neepbus"),
                ChannelFormat.RUNNABLE, playerInventory.player);

        checkContainerSize(inventory, 1);

        this.filterSlots = createSlotBlock(1, inventory, 0, PatternSlot::new);
        this.playerSlots = createPlayerSlots(8, 96, playerInventory);

        if (!playerInventory.player.level().isClientSide())
            openConfigC2S.receiver(() -> playerInventory.player.openMenu(NeepBusConfigScreenHandler.getFactory(config)));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        ItemStack oldStack = getItems().get(index);
        inventory.setItem(0, oldStack.copy());
        return ItemStack.EMPTY;
    }
}
