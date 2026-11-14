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

import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.util.MiscUtil;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.text.DecimalFormat;

public record TankMessagePacket(BlockPos pos, long amount, ResourceLocation fluid) implements CustomPacketPayload {
    public static final Type<TankMessagePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "tank_message"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TankMessagePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TankMessagePacket::pos,
            ByteBufCodecs.VAR_LONG, TankMessagePacket::amount,
            ResourceLocation.STREAM_CODEC, TankMessagePacket::fluid,
            TankMessagePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public Component contents() {
        Fluid stored = BuiltInRegistries.FLUID.getOptional(fluid).orElse(Fluids.EMPTY);
        if (stored == Fluids.EMPTY || amount <= 0) {
            return Component.literal("Empty");
        }
        MutableComponent name = Component.translatable(stored.getFluidType().getDescriptionId());
        double buckets = (double) amount / FluidConstants.BUCKET;
        if (buckets > 10) {
            return name.append(": " + new DecimalFormat("###.###").format(buckets) + "b");
        }
        return name.append(": " + MiscUtil.dropletsToMb(amount) + "mb");
    }
}
