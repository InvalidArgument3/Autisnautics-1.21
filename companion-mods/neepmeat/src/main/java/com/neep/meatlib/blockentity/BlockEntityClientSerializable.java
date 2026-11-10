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

package com.neep.meatlib.blockentity;

import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

// I couldn't live without this interface
public interface BlockEntityClientSerializable
{
    void fromClientTag(CompoundTag nbt);

    void toClientTag(CompoundTag nbt);

    default void sync()
    {
        if (this instanceof BlockEntity be)
        {
            be.setChanged();
            if (be.getLevel() instanceof ServerLevel serverWorld)
            {
//                be.getWorld().updateListeners(be.getPos(), be.getCachedState(), be.getCachedState(), Block.NOTIFY_LISTENERS);
                serverWorld.getChunkSource().blockChanged(be.getBlockPos());
            }
        }
    }

    default CompoundTag createSyncNbt()
    {
        if (this instanceof BlockEntity blockEntity)
        {
            HolderLookup.Provider registries = blockEntity.getLevel() != null
                    ? blockEntity.getLevel().registryAccess() : net.minecraft.core.RegistryAccess.EMPTY;
            return blockEntity.getUpdateTag(registries);
        }
        return new CompoundTag();
    }

    /**
     * Override to apply baked model updates after sync.
     */
    default void onReceiveNbt(CompoundTag nbt) { }

    default void sendUpdatePacket(List<ServerPlayer> players)
    {
        if (this instanceof BlockEntity be)
        {
            // The packet is ClientboundBlockEntityDataPacket from SyncableBlockEntity.sync.
        }
    }

}
