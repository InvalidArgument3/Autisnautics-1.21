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

package com.neep.neepmeat.api.processing;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.network.PacketBufUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;

public record OreFatSyncData(Map<ResourceLocation, OreFatRegistry.Entry> nbtToEntry)
{
    public static final ParamCodec<OreFatSyncData> PARAM_CODEC = ParamCodec.of(OreFatSyncData.class, OreFatSyncData::write, OreFatSyncData::fromBuf);

    public static OreFatSyncData fromBuf(FriendlyByteBuf buf)
    {
        Map<ResourceLocation, OreFatRegistry.Entry> map = new HashMap<>();
        PacketBufUtil.readMap(buf, map::put, FriendlyByteBuf::readResourceLocation, OreFatRegistry.Entry::read);

        return new OreFatSyncData(map);
    }

    public void write(FriendlyByteBuf buf)
    {
        PacketBufUtil.writeMap(buf, nbtToEntry, (k, b) -> b.writeResourceLocation(k), OreFatRegistry.Entry::write);
    }

    @Environment(EnvType.CLIENT)
    public static class Client
    {
        public static void init()
        {
            OreFatRegistry.SYNC.receiverHandler(EnvType.CLIENT, (player, buf, responseSender) ->
                    OreFatRegistry.INSTANCE::onPacket);
        }
    }

    public interface SyncOreFats
    {
        void apply(OreFatSyncData data);
    }
}
