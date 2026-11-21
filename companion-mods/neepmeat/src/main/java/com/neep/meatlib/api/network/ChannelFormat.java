/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.api.network;

import com.neep.meatlib.network.ChannelFormatFormatImpl;
import com.neep.meatlib.network.Sender;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.network.FriendlyByteBuf;

import java.util.concurrent.Executor;
import java.util.function.IntConsumer;

public interface ChannelFormat<T> {
    ChannelFormat<Runnable> RUNNABLE = builder(Runnable.class).build();
    ChannelFormat<IntConsumer> INT = builder(IntConsumer.class).param(ParamCodec.INT).build();
    ChannelFormat<BooleanConsumer> BOOLEAN = builder(BooleanConsumer.class).param(ParamCodec.BOOLEAN).build();

    static <T> ChannelFormatFormatImpl.Builder<T> builder(Class<T> clazz) {
        return new ChannelFormatFormatImpl.Builder<>(clazz);
    }

    void receive(T listener, FriendlyByteBuf buf, Executor executor);

    T emitter(Sender<T> sender);
}
