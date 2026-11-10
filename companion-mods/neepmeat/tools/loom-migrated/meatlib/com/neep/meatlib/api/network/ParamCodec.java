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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.IntStream;

// Ooooh, wow. Look at all the amazing things you can do with Mojang Datafixers.
// But you can't use them to spit out stuff in the desired order.
// So here's a shitty version that uses reflection.
public interface ParamCodec<T>
{
    Class<T> clazz();

    T decode(FriendlyByteBuf buf);

    void encode(T t, FriendlyByteBuf buf);

    ParamCodec<Integer> INT = of(int.class, (o, buf) -> buf.writeInt(o), FriendlyByteBuf::readInt);
    ParamCodec<Long> LONG = of(long.class, (o, buf) -> buf.writeLong(o), FriendlyByteBuf::readLong);
    ParamCodec<Float> FLOAT = of(float.class, (o, buf) -> buf.writeFloat(o), FriendlyByteBuf::readFloat);
    ParamCodec<Double> DOUBLE = of(double.class, (o, buf) -> buf.writeDouble(o), FriendlyByteBuf::readDouble);
    ParamCodec<Boolean> BOOLEAN = of(boolean.class, (o, buf) -> buf.writeBoolean(o), FriendlyByteBuf::readBoolean);
    ParamCodec<String> STRING = of(String.class, (o, buf) -> buf.writeUtf(o), FriendlyByteBuf::readUtf);
    ParamCodec<UUID> UUID = of(UUID.class, (o, buf) -> buf.writeUUID(o), FriendlyByteBuf::readUUID);
    ParamCodec<CompoundTag> NBT = of(CompoundTag.class, (o, buf) -> buf.writeNbt(o), FriendlyByteBuf::readNbt);

    ParamCodec<ByteBuf> BYTE_BUF = of(ByteBuf.class, (o, buf) ->
    {
        buf.writeVarInt(o.writerIndex());
        buf.writeBytes(o);
    }, buf ->
    {
        int toRead = buf.readVarInt();
        var newBb = Unpooled.buffer();
        newBb.capacity(toRead);
        buf.readBytes(newBb, toRead);
        return newBb;
    });

    ParamCodec<FriendlyByteBuf> BUF = of(FriendlyByteBuf.class, (o, buf) ->
    {
        if (o != null)
        {
            buf.writeBoolean(true);
            buf.writeVarInt(o.readableBytes()); // Hopefully this works.
            buf.asByteBuf().writeBytes(o.asByteBuf());
        }
        else
        {
            buf.writeBoolean(false);
        }
    }, buf ->
    {
        boolean present = buf.readBoolean();
        if (present)
        {
            int bytes = buf.readVarInt();
            return PacketByteBufs.copy(PacketByteBufs.readSlice(buf, bytes));
        }
        return null;
    });

    ParamCodec<FloatList> FLOAT_LIST = of(FloatList.class, (o, buf) ->
    {
        buf.writeVarInt(o.size());
        for (float f : o)
        {
            buf.writeFloat(f);
        }
    }, buf ->
    {
        // Array iteration is faster than a stream, right?
        int size = buf.readVarInt();
        float[] floats = new float[size];
        for (int i = 0; i < size; ++i)
        {
            floats[i] = buf.readFloat();
        }
        return FloatList.of(floats);
    });

    ParamCodec<IntList> INT_LIST = of(IntList.class, (o, buf) ->
    {
        buf.writeVarInt(o.size());
        for (int i : o)
        {
            buf.writeInt(i);
        }
    }, buf ->
    {
        // Array iteration is faster than a stream, right?
        // No point in using fastutil types unless everything else is fAsT
        // Gotta go fast
        // Gotta prematurely optimise
        // Sunk cost fallacy
        int size = buf.readVarInt();
        int[] ints = new int[size];
        for (int i = 0; i < size; ++i)
        {
            ints[i] = buf.readInt();
        }
        return IntList.of(ints);
    });

    static <T> ParamCodec<List<T>> list(ParamCodec<T> codec)
    {
        return ParamCodec.<List<T>>of((Class<List<T>>) (Object) List.class, (o, buf) ->
        {
            buf.writeInt(o.size());
            for (T t : o)
            {
                codec.encode(t, buf);
            }
        },
        buf ->
        {
            int size = buf.readInt();
            return IntStream.range(0, size).mapToObj(i -> codec.decode(buf)).toList();
        });
    }

    static <T extends Enum<T>> ParamCodec<T> ofEnum(Class<T> tEnum)
    {
        return of(tEnum, (t, buf) -> buf.writeVarInt(t.ordinal()), buf -> tEnum.getEnumConstants()[buf.readVarInt()]);
    }

    static <T> ParamCodec<T> of(Class<T> clazz, Encoder<T> encoder, Decoder<T> decoder)
    {
        return new ParamCodec<T>()
        {
            @Override
            public Class<T> clazz()
            {
                return clazz;
            }

            @Override
            public T decode(FriendlyByteBuf buf)
            {
                return decoder.decode(buf);
            }

            @Override
            public void encode(T t, FriendlyByteBuf buf)
            {
                encoder.encode(t, buf);
            }
        };
    }

    static <T> ParamCodec<T> ofNullable(ParamCodec<T> codec)
    {
        return of(codec.clazz(), (o, b) ->
        {
            boolean present = o != null;
            b.writeBoolean(present);

            if (present)
                codec.encode(o, b);

        }, b ->
        {
            boolean present = b.readBoolean();
            if (present)
                return codec.decode(b);

            return null;
        });
    }

    default <T1> ParamCodec<T1> map(Class<T1> t1Class, Function<T1, T> f1, Function<T, T1> f2)
    {
        return ParamCodec.of(t1Class, (o, b) -> encode(f1.apply(o), b), b -> f2.apply(decode(b)));
    }

    default ParamCodec<List<T>> listOf()
    {
        return list(this);
    }


    interface Encoder<T>
    {
        void encode(T t, FriendlyByteBuf buf);
    }

    interface Decoder<T>
    {
        T decode(FriendlyByteBuf buf);
    }
}
