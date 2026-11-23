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

package com.neep.neepbus.network;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.network.GlobalChannelManager;
import com.neep.neepbus.NeepBus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class NeepBusNetwork
{
    public static final ParamCodec<BlockPos> BLOCK_POS_PARAM_CODEC = ParamCodec.of(BlockPos.class, (o, buf) -> buf.writeBlockPos(o), FriendlyByteBuf::readBlockPos);

    public static final GlobalChannelManager<NetworkingToolConnect> NT_CONNECT = GlobalChannelManager.create(
            ResourceLocation.fromNamespaceAndPath(NeepBus.REGISTRY_NAMESPACE, "nt_connect"),
            ChannelFormat.builder(NetworkingToolConnect.class)
                    .param(BLOCK_POS_PARAM_CODEC)
                    .param(ParamCodec.BOOLEAN)
                    .param(ParamCodec.INT)
                    .param(ParamCodec.STRING)
                    .build());

    public static final GlobalChannelManager<OpenConfigScreen> OPEN_CONFIG_SCREEN = GlobalChannelManager.create(
            ResourceLocation.fromNamespaceAndPath(NeepBus.REGISTRY_NAMESPACE, "nt_config_screen"),
            ChannelFormat.builder(OpenConfigScreen.class).param(BLOCK_POS_PARAM_CODEC).build());


    @FunctionalInterface
    public interface NetworkingToolConnect
    {
        void send(BlockPos pos, boolean isOutput, int entryIndex, String address);
    }

    public interface OpenConfigScreen
    {
        void open(BlockPos pos);
    }
}
