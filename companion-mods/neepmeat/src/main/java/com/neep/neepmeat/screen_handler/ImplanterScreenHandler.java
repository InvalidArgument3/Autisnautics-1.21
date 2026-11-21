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
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.plc.block.entity.ImplanterBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class ImplanterScreenHandler extends BasicScreenHandler
{
    private final ImplanterBlockEntity be;

    public final ChannelManager<ApplyImplant> applyC2S;


    public ImplanterScreenHandler(Inventory playerInventory, Container inventory, int syncId, ImplanterBlockEntity be)
    {
        super(NMScreenHandlers.IMPLANTER, playerInventory, inventory, syncId, null);
        this.be = be;
        createPlayerSlots(0, 0, playerInventory);
        createSlotBlock(0, 0, 1, 1, inventory, 0, (inventory1, index, x, y) -> new Slot(inventory1, index, x, y));

        applyC2S = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "implanter/apply_implant"),
                ChannelFormat.builder(ApplyImplant.class).build(), playerInventory.player);

        if (!playerInventory.player.level().isClientSide())
            applyC2S.receiver(this::applyImplant);
    }

    private void applyImplant()
    {
        be.startApplySequenceManual();
    }

    public ImplanterScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf)
    {
        this(playerInventory, new Pain(), syncId, (ImplanterBlockEntity) playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ImplanterBlockEntity getImplanter()
    {
        return be;
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        applyC2S.close();
    }

    public interface ApplyImplant
    {
        void apply();
    }

    private static class Pain extends SimpleContainer
    {
        public Pain()
        {
            super(1);
        }

        @Override
        public int getMaxStackSize()
        {
            return 1;
        }
    }
}
