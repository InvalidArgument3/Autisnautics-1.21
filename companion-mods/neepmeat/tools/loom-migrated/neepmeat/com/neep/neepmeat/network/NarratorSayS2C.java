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

package com.neep.neepmeat.network;

import com.mojang.text2speech.Narrator;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.network.GlobalChannelManager;
import com.neep.neepmeat.NeepMeat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

public class NarratorSayS2C
{
    public static final GlobalChannelManager<Say> CHANNEL = GlobalChannelManager.create(new ResourceLocation(NeepMeat.NAMESPACE, "narrator_say"),
            ChannelFormat.builder(Say.class).param(ParamCodec.STRING).param(ParamCodec.BOOLEAN).build());

    public interface Say
    {
        void apply(String msg, boolean interrupt);
    }

    @Environment(EnvType.CLIENT)
    public static class Client
    {
        public static void init()
        {
            CHANNEL.receiver(EnvType.CLIENT, (msg, interrupt) ->
            {
                Narrator.getNarrator().say(msg, interrupt);
            });
        }
    }
}
