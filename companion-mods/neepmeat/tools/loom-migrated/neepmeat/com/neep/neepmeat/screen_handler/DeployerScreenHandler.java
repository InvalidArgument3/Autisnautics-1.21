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
import com.neep.neepmeat.machine.deployer.DeployerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public class DeployerScreenHandler extends BasicScreenHandler implements ScreenReadyListener
{
    public final ChannelManager<UpdateValues> updateS2CC2S;
    private boolean ready;
    @Nullable private final DeployerBlockEntity be;

    public DeployerScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, new SimpleContainer(9), null, syncId);
    }

    public DeployerScreenHandler(Inventory playerInventory, Container inventory, @Nullable DeployerBlockEntity be, int syncId)
    {
        super(NMScreenHandlers.DEPLOYER, playerInventory, inventory, syncId, null);
        this.be = be;

        createSlotBlock(19, 19, 3, 3, inventory, 0, Slot::new);

        createPlayerSlots(8, 91 + 8, playerInventory);
        updateS2CC2S = ChannelManager.create(new ResourceLocation(NeepMeat.NAMESPACE, "deployer_update"),
                ChannelFormat.builder(UpdateValues.class).param(ParamCodec.BOOLEAN).param(ParamCodec.BOOLEAN).build(),
                playerInventory.player);

        if (!isClient())
            updateS2CC2S.receiver(this::receiveValues);

    }

    private void receiveValues(boolean roundRobin, boolean interactBlock)
    {
        if (be != null)
        {
            be.roundRobin = roundRobin;
            be.interactBlock = interactBlock;
        }
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();
        if (ready && be != null)
            updateS2CC2S.emitter().update(be.roundRobin, be.interactBlock);
    }

    @Override
    public void onScreenReady()
    {
        ready = true;
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        updateS2CC2S.close();
    }

    public interface UpdateValues
    {
        void update(boolean roundRobin, boolean interactBlock);
    }
}
