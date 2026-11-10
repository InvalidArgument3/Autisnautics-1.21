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
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public final class TankBlockEntity {
    private TankBlockEntity() {}

    /**
     * Upstream sends {@code TankMessagePacket}. The client packet is not ported, so the
     * same sound plays and the amount is shown on the action bar.
     */
    public static void showContents(ServerPlayer player, Level world, BlockPos pos, StorageView<FluidVariant> buffer) {
        world.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1f, 1.5f);
        player.displayClientMessage(Component.literal(buffer.getAmount() + " " + BuiltInRegistries.FLUID.getKey(buffer.getResource().fluid())), true);
    }
}
