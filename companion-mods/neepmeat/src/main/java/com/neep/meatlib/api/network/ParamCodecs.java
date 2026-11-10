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

import com.neep.meatlib.network.PacketBufUtil;
import com.neep.meatlib.recipe.MeatlibRecipe;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.TransferVariant;
import com.neep.meatlib.transfer.ResourceAmount;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ParamCodecs
{
    public static final ParamCodec<BlockPos> BLOCK_POS = ParamCodec.of(BlockPos.class,
            (o, buf) -> buf.writeBlockPos(o), FriendlyByteBuf::readBlockPos);
    public static final ParamCodec<ResourceLocation> IDENTIFIER = ParamCodec.of(ResourceLocation.class, (o, buf) -> buf.writeResourceLocation(o), FriendlyByteBuf::readResourceLocation);
    public static final ParamCodec<Vec3> VEC3D = ParamCodec.of(Vec3.class, (o, b) -> PacketBufUtil.writeVec3d(b, o), PacketBufUtil::readVec3d);
    public static final ParamCodec<ItemVariant> ITEM_VARIANT = ParamCodec.of(ItemVariant.class, TransferVariant::toPacket, ItemVariant::fromPacket);
    public static final ParamCodec<ItemStack> ITEM_STACK = ParamCodec.NBT.map(ItemStack.class, s -> s.save(new CompoundTag()), ItemStack::of);
    public static final ParamCodec<Component> TEXT_AS_STRING = ParamCodec.STRING.map(Component.class, Component::getString, Component::nullToEmpty);

    public static <T> ParamCodec<T> registryValue(Class<T> clazz, Registry<T> registry)
    {
        return ParamCodec.of(clazz, (o, b) ->
        {
            b.writeId(registry, o);
        }, b ->
        {
            return b.readById(registry);
        });
    }

    public static <T> Class<T> as(Class<?> clazz)
    {
        return (Class<T>) clazz;
    }

    public static <T> ParamCodec<? extends T> constant(Class<T> clazz, T object)
    {
        return ParamCodec.of(clazz, (o, buf) -> {},
                b -> object);
    }

    public static <K, V> ParamCodec<Map<K, V>> map(Class<Map<K, V>> clazz, ParamCodec<K> keyCodec, ParamCodec<V> valueCodec)
    {
        return ParamCodec.of(clazz,
                (o, buf) -> PacketBufUtil.writeMap(buf, o, keyCodec::encode, valueCodec::encode),
                buf ->
                {
                    Map<K, V> result = new HashMap<>();
                    PacketBufUtil.readMap(buf, result::put, keyCodec::decode, valueCodec::decode);
                    return result;
                }
        );
    }

    public static <T extends MeatlibRecipe<?>> ParamCodec<T> ofRecipe(Class<T> clazz, RecipeSerializer<T> serializer)
    {
        return ParamCodec.of(clazz, (o, b) ->
        {
            b.writeResourceLocation(o.getId());
            serializer.toNetwork(b, o);
        }, b ->
        {
            ResourceLocation id = b.readResourceLocation();
            return serializer.fromNetwork(id, b);
        });
    }

    public static <T extends TransferVariant<?>> ParamCodec<ResourceAmount<T>> resourceAmount(Function<FriendlyByteBuf, T> decode)
    {
        return ParamCodec.of(as(ResourceAmount.class), (o, b) ->
        {
            o.resource().toPacket(b);
            b.writeVarLong(o.amount());
        }, b ->
                new ResourceAmount<>(decode.apply(b), b.readVarLong()));
    }
}
