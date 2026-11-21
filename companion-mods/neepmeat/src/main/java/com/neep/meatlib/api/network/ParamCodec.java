/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.api.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Ordered codec used by {@link ChannelFormat}. Upstream wrote this because DFU codecs
 * do not preserve call order for the dynamic-proxy channels. The same shape is kept,
 * on {@link FriendlyByteBuf}.
 */
public interface ParamCodec<T> {
    Class<T> clazz();

    T decode(FriendlyByteBuf buf);

    void encode(T value, FriendlyByteBuf buf);

    ParamCodec<Integer> INT = of(int.class, (value, buf) -> buf.writeInt(value), FriendlyByteBuf::readInt);
    ParamCodec<Long> LONG = of(long.class, (value, buf) -> buf.writeLong(value), FriendlyByteBuf::readLong);
    ParamCodec<Float> FLOAT = of(float.class, (value, buf) -> buf.writeFloat(value), FriendlyByteBuf::readFloat);
    ParamCodec<Double> DOUBLE = of(double.class, (value, buf) -> buf.writeDouble(value), FriendlyByteBuf::readDouble);
    ParamCodec<Boolean> BOOLEAN = of(boolean.class, (value, buf) -> buf.writeBoolean(value), FriendlyByteBuf::readBoolean);
    ParamCodec<String> STRING = of(String.class, (value, buf) -> buf.writeUtf(value), FriendlyByteBuf::readUtf);
    ParamCodec<UUID> UUID = of(UUID.class, (value, buf) -> buf.writeUUID(value), buf -> buf.readUUID());
    ParamCodec<CompoundTag> NBT = of(CompoundTag.class, (value, buf) -> buf.writeNbt(value), buf -> buf.readNbt());
    ParamCodec<ByteBuf> BYTE_BUF = of(ByteBuf.class, (value, buf) -> {
        buf.writeVarInt(value.writerIndex());
        buf.writeBytes(value);
    }, buf -> {
        int toRead = buf.readVarInt();
        ByteBuf copy = Unpooled.buffer();
        copy.capacity(toRead);
        buf.readBytes(copy, toRead);
        return copy;
    });

    default <R> ParamCodec<R> map(Function<T, R> to, Function<R, T> from) {
        ParamCodec<T> self = this;
        return new ParamCodec<>() {
            @Override
            public Class<R> clazz() {
                return null;
            }

            @Override
            public R decode(FriendlyByteBuf buf) {
                return to.apply(self.decode(buf));
            }

            @Override
            public void encode(R value, FriendlyByteBuf buf) {
                self.encode(from.apply(value), buf);
            }
        };
    }

    static <T extends Enum<T>> ParamCodec<T> ofEnum(Class<T> clazz) {
        return of(clazz, (value, buf) -> buf.writeEnum(value), buf -> buf.readEnum(clazz));
    }

    static <T> ParamCodec<T> of(Class<T> clazz, BiConsumer<T, FriendlyByteBuf> encode, Function<FriendlyByteBuf, T> decode) {
        return new ParamCodec<>() {
            @Override
            public Class<T> clazz() {
                return clazz;
            }

            @Override
            public T decode(FriendlyByteBuf buf) {
                return decode.apply(buf);
            }

            @Override
            public void encode(T value, FriendlyByteBuf buf) {
                encode.accept(value, buf);
            }
        };
    }
}
