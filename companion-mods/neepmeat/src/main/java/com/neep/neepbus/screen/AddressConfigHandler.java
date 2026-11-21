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

package com.neep.neepbus.screen;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.api.network.RecordParamCodec;
import com.neep.meatlib.network.ChannelManager;
import com.neep.meatlib.screen.WidgetHandler;
import com.neep.neepbus.util.ConfigEntry;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepbus.util.NeepBusConfig;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class AddressConfigHandler implements WidgetHandler
{
    public static final ParamCodec<List<SyncEntry>> SYNC_ENTRY_CODEC = ParamCodec.list(SyncEntry.PARAM_CODEC);
    public static final ChannelFormat<NeepBusConfigScreenHandler.UpdateEntries> SYNC_ENTRIES_FORMAT = ChannelFormat.builder(NeepBusConfigScreenHandler.UpdateEntries.class)
            .param(SYNC_ENTRY_CODEC).build();

    public final ChannelManager<NeepBusConfigScreenHandler.OnAddressChange> addressChangeC2S;
    public final ChannelManager<NeepBusConfigScreenHandler.UpdateEntries> updateInputsS2C;
    public final ChannelManager<NeepBusConfigScreenHandler.UpdateEntries> updateOutputsS2C;

    // Populated in the client constructor
    public List<AddressConfigHandler.SyncEntry> outputs = List.of();
    public List<AddressConfigHandler.SyncEntry> inputs = List.of();

    protected final NeepBusConfig config;

    public AddressConfigHandler(NeepBusConfig config, Player player)
    {
        this.config = config;

        this.addressChangeC2S = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "address_change"),
                ChannelFormat.builder(NeepBusConfigScreenHandler.OnAddressChange.class)
                        .param(ParamCodec.BOOLEAN)
                        .param(ParamCodec.INT)
                        .param(ParamCodec.STRING)
                        .build(),
                player);

        this.updateInputsS2C = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "update_entries_inputs"),
                SYNC_ENTRIES_FORMAT,
                player);

        this.updateOutputsS2C = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "update_entries_outputs"),
                SYNC_ENTRIES_FORMAT,
                player);

        addressChangeC2S.receiver(this::onAddressChange);

        updateInputsS2C.receiver(this::receiveInputs);
        updateOutputsS2C.receiver(this::receiveOutputs);
    }

    public void receiveOpeningData(FriendlyByteBuf buf)
    {
        receiveInputs(AddressConfigHandler.SYNC_ENTRY_CODEC.decode(buf));
        receiveOutputs(AddressConfigHandler.SYNC_ENTRY_CODEC.decode(buf));
    }

    private void receiveInputs(List<SyncEntry> list)
    {
        this.inputs = list;
    }

    private void receiveOutputs(List<SyncEntry> list)
    {
        this.outputs = list;
    }

    private void onAddressChange(boolean output, int idx, String newAddress)
    {
        if (config != null) // Just in case // REMOVE
        {
            if (!output)
            {
                config.getInputs().get(idx).setAddress(newAddress);
            }
            else
            {
                config.getOutputs().get(idx).setAddress(newAddress);
            }
            config.applyChanges();
        }
    }

    @Override
    public void close()
    {
        addressChangeC2S.close();
        updateInputsS2C.close();
        updateOutputsS2C.close();
    }

    public void sendUpdates()
    {
        if (config != null)
        {
            updateInputsS2C.emitter().update(config.getInputs().stream().map(AddressConfigHandler.SyncEntry::of).toList());
            updateOutputsS2C.emitter().update(config.getOutputs().stream().map(AddressConfigHandler.SyncEntry::of).toList());
        }
    }

    public NeepBusConfig config()
    {
        return config;
    }

    // A simple container for a name and address that can be easily serialised
    public record SyncEntry(String name, String address, Component tooltip)
    {
        public static SyncEntry of(ConfigEntry entry)
        {
            return new SyncEntry(entry.getName(), entry.getAddress(), entry.getTooltip());
        }

        public static final ParamCodec<SyncEntry> PARAM_CODEC = RecordParamCodec.builder(SyncEntry.class)
                .param(ParamCodec.STRING).param(ParamCodec.STRING).param(ParamCodecs.TEXT_AS_STRING).build();
    }
}
