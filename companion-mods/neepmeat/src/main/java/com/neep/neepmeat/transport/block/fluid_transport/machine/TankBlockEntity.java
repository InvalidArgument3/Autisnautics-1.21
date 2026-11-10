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

package com.neep.neepmeat.transport.block.fluid_transport.machine;

import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.StorageView;
import com.neep.neepmeat.network.TankMessagePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TankBlockEntity {
    private TankBlockEntity() {}

    /** Plays the inspection sound and sends {@link TankMessagePacket} for the action-bar text. */
    public static void showContents(ServerPlayer player, Level world, BlockPos pos, StorageView<FluidVariant> buffer) {
        world.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1f, 1.5f);
        PacketDistributor.sendToPlayer(player, new TankMessagePacket(pos, buffer.getAmount(), BuiltInRegistries.FLUID.getKey(buffer.getResource().fluid())));
    }
}
