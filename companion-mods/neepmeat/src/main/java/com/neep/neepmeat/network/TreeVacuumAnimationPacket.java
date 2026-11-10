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

import com.neep.neepmeat.NeepMeat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TreeVacuumAnimationPacket(BlockPos pos, boolean playSound) implements CustomPacketPayload {
    public static final Type<TreeVacuumAnimationPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "tree_vacuum_anim"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TreeVacuumAnimationPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TreeVacuumAnimationPacket::pos,
            ByteBufCodecs.BOOL, TreeVacuumAnimationPacket::playSound,
            TreeVacuumAnimationPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
