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

package com.neep.meatlib.mixin;

import com.neep.meatlib.blockentity.BlockEntityClientSerializable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(ChunkHolder.class)
public abstract class ChunkHolderMixin
{
    @Shadow protected abstract void sendPacketToPlayers(List<ServerPlayer> players, Packet<?> packet);

    @Inject(method = "sendBlockEntityUpdatePacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;toUpdatePacket()Lnet/minecraft/network/packet/Packet;"), cancellable = true,
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void sendBECSUpdatePacket(List<ServerPlayer> players, Level world, BlockPos pos, CallbackInfo ci, BlockEntity blockEntity)
    {
        if (blockEntity instanceof BlockEntityClientSerializable becs)
        {
            Packet<?> packet = blockEntity.getUpdatePacket();
            if (packet != null)
            {
                sendPacketToPlayers(players, packet);
            }
            else
            {
                becs.sendUpdatePacket(players);
            }

            ci.cancel();
        }
    }
}
