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

package com.neep.meatlib.api.network;

import com.neep.meatlib.network.ChannelFormatFormatImpl;
import com.neep.meatlib.network.Sender;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.concurrent.Executor;
import java.util.function.IntConsumer;
import net.minecraft.network.FriendlyByteBuf;

public interface ChannelFormat<T>
{
    ChannelFormat<Runnable> RUNNABLE = builder(Runnable.class).build();
    ChannelFormat<IntConsumer> INT = builder(IntConsumer.class).param(ParamCodec.INT).build();
    ChannelFormat<BooleanConsumer> BOOLEAN = builder(BooleanConsumer.class).param(ParamCodec.BOOLEAN).build();

    static <T> ChannelFormatFormatImpl.Builder<T> builder(Class<T> clazz)
    {
        return new ChannelFormatFormatImpl.Builder<>(clazz);
    }

    void receive(T listener, FriendlyByteBuf buf, Executor executor);

    T emitter(Sender<T> sender);
}
