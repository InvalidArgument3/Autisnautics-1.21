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

package com.neep.meatlib.network;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class PacketBufUtil
{

    /**
     * Packs a floating-point angle into a {@code byte}.
     *
     * @param angle angle
     * @return packed angle
     */
    public static byte packAngle(float angle)
    {
        return (byte) Mth.floor(angle * 256 / 360);
    }

    /**
     * Unpacks a floating-point angle from a {@code byte}.
     *
     * @param angleByte packed angle
     * @return angle
     */
    public static float unpackAngle(byte angleByte)
    {
        return (angleByte * 360) / 256f;
    }

    /**
     * Writes an angle to a {@link FriendlyByteBuf}.
     *
     * @param buf destination buffer
     * @param angle   angle
     */
    public static void writeAngle(FriendlyByteBuf buf, float angle)
    {
        buf.writeByte(packAngle(angle));
    }

    /**
     * Reads an angle from a {@link FriendlyByteBuf}.
     *
     * @param buf source buffer
     * @return angle
     */
    public static float readAngle(FriendlyByteBuf buf)
    {
        return unpackAngle(buf.readByte());
    }

    /**
     * Writes a {@link Vec3} to a {@link FriendlyByteBuf}.
     *
     * @param buf destination buffer
     * @param vec3d   vector
     */
    public static void writeVec3d(FriendlyByteBuf buf, Vec3 vec3d)
    {
        buf.writeDouble(vec3d.x);
        buf.writeDouble(vec3d.y);
        buf.writeDouble(vec3d.z);
    }

    /**
     * Reads a {@link Vec3} from a {@link FriendlyByteBuf}.
     *
     * @param buf source buffer
     * @return vector
     */
    public static Vec3 readVec3d(FriendlyByteBuf buf)
    {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        return new Vec3(x, y, z);
    }

    /**
     * Writes a {@link BlockPos} to a {@link FriendlyByteBuf}.
     *
     * @param buf destination buffer
     * @param pos   pos
     */
    public static void writeBlockPos(FriendlyByteBuf buf, BlockPos pos)
    {
        buf.writeVarInt(pos.getX());
        buf.writeVarInt(pos.getY());
        buf.writeVarInt(pos.getZ());
    }

    /**
     * Reads a {@link BlockPos} from a {@link FriendlyByteBuf}.
     *
     * @param buf source buffer
     * @return vector
     */
    public static BlockPos readBlockPos(FriendlyByteBuf buf)
    {
        return new BlockPos(
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt());
    }

    public static <K, V> void writeMap(FriendlyByteBuf buf, Map<K, V> map, BiConsumer<K, FriendlyByteBuf> writeKey, BiConsumer<V, FriendlyByteBuf> writeVal)
    {
        buf.writeVarInt(map.size());
        map.forEach((key, val) ->
        {
            writeKey.accept(key, buf);
            writeVal.accept(val, buf);
        });
    }

    public static <K, V> void readMap(FriendlyByteBuf buf, BiConsumer<K, V> add, Function<FriendlyByteBuf, K> readKey, Function<FriendlyByteBuf, V> readVal)
    {
        int size = buf.readVarInt();
        for (int i = 0; i < size; ++i)
        {
            K key = readKey.apply(buf);
            V val = readVal.apply(buf);
            add.accept(key, val);
        }
    }

    public static <T> void writeList(FriendlyByteBuf buf, List<T> list, BiConsumer<FriendlyByteBuf, T> writer)
    {
        buf.writeVarInt(list.size());
        for (var element : list)
        {
            writer.accept(buf, element);
        }
    }

    public static <T> List<T> readList(FriendlyByteBuf buf, Function<FriendlyByteBuf, T> reader)
    {
        int size = buf.readVarInt();
        return IntStream.range(0, size).mapToObj(i -> reader.apply(buf)).toList();
    }
}
