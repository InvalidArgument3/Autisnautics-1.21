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

import com.neep.meatlib.network.BlockEntitySync;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A replacement for `BlockEntityClientSerializable` that was removed at some point.
 * It provides a way to separate the data sent to the client with the data from the data that is persistently saved.
 * toClientTag and fromClientTag delegate to writeNbt and readNbt respectively.
 * Override them to selectively save and send data.
 *
 * Also removes the call to {@link World$updateComparators} when markDirty is called.
 */
public abstract class SyncableBlockEntity extends BlockEntity implements BlockEntityClientSerializable
{
    private boolean updateComparators;
    private boolean dirty;

    private final List<BlockEntityInvalidateListener> listeners = new ObjectArrayList<>();

    public SyncableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    /**
     * Add a resource that is invalidated when {@link BlockEntity#setBlockState(BlockState)} is called.
     * Used for DynamicBlockApiCache and AutoEjector
     */
    protected <T extends BlockEntityInvalidateListener> T addListener(T listener)
    {
        listeners.add(listener);
        return listener;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        // Return null to enable custom behaviour.
        return null;
    }

    @Override
    public void sendUpdatePacket(List<ServerPlayer> players)
    {
        BlockEntitySync.send(players, this);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag nbt = new CompoundTag();
        toClientTag(nbt);
        return nbt;
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
    }

    @Override
    public void sync()
    {
        this.setChanged();
        softSync();
    }

    public void softSync()
    {
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        if (level instanceof ServerLevel serverWorld)
        {
//            serverWorld.getChunkManager().markForUpdate(pos);
        }
    }

    @Override
    public void fromClientTag(CompoundTag nbt)
    {
        load(nbt);
    }

    /**
     * Default behaviour is to serialise and send all data to the client.
     * Override to selectively send properties.
     */
    @Override
    public void toClientTag(CompoundTag nbt)
    {
        saveAdditional(nbt);
    }

    @Override
    public void setChanged()
    {
        if (this.level != null)
        {
            level.blockEntityChanged(worldPosition);
            this.updateComparators = true;

            // New behaviour that doesn't cause thousands of stupid comparator updates
            level.getChunkAt(worldPosition).setUnsaved(true);
        }
    }

    // This can be called once every tick
    public void tryUpdateComparators()
    {
        if (updateComparators)
        {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            updateComparators = false;
        }
    }

    @Override
    public void setBlockState(BlockState state)
    {
        super.setBlockState(state);

        for (BlockEntityInvalidateListener listener : listeners)
        {
            listener.invalidate();
        }
    }

    //    public static class SyncPacket implements Packet<Client>
//    {
//        private final BlockPos pos;
//        private final BlockEntityType<?> type;
//        private final NbtCompound nbt;
//
//        public SyncPacket(SyncableBlockEntity syncableBlockEntity)
//        {
//            this(syncableBlockEntity.getPos(), syncableBlockEntity.getType(), syncableBlockEntity.toInitialChunkDataNbt());
//        }
//
//        public SyncPacket(BlockPos pos, BlockEntityType<?> type, NbtCompound nbt)
//        {
//            this.pos = pos;
//            this.type = type;
//            this.nbt = nbt.isEmpty() ? null : nbt;
//        }
//
//        public SyncPacket(PacketByteBuf buf)
//        {
//            this.pos = buf.readBlockPos();
//            this.type = buf.readRegistryValue(Registries.BLOCK_ENTITY_TYPE);
//            this.nbt = buf.readNbt();
//        }
//
//        @Override
//        public void write(PacketByteBuf buf)
//        {
//            buf.writeBlockPos(this.pos);
//            buf.writeRegistryValue(Registries.BLOCK_ENTITY_TYPE, this.type);
//            buf.writeNbt(this.nbt);
//        }
//
//        @Override
//        public void apply(Client listener)
//        {
//            Client.getInstance().apply(this);
//        }
//
//        public BlockPos getPos()
//        {
//            return pos;
//        }
//
//        public BlockEntityType<?> getType()
//        {
//            return type;
//        }
//
//        public NbtCompound getNbt()
//        {
//            return nbt;
//        }
//    }
//
//    private interface ClientPacketListener extends PacketListener
//    {
//        void apply(SyncPacket packet);
//    }
//
//    @Environment(EnvType.CLIENT)
//    private static class Client implements ClientPacketListener
//    {
//        private static final Client INSTANCE = new Client();
//        private final MinecraftClient client;
//
//        public static Client getInstance()
//        {
//            return INSTANCE;
//        }
//
//        private Client()
//        {
//            this.client = MinecraftClient.getInstance();
//        }
//
//        @Override
//        public void apply(SyncPacket packet)
//        {
//            NetworkThreadUtils.forceMainThread(packet, this, this.client);
//            BlockPos blockPos = packet.getPos();
//            this.client.world.getBlockEntity(blockPos, packet.getType()).ifPresent((blockEntity) ->
//            {
//                NbtCompound nbt = packet.getNbt();
//                if (nbt != null)
//                {
//                    ((SyncableBlockEntity) blockEntity).fromClientTag(nbt);
//                }
//            });
//        }
//
//        @Override
//        public void onDisconnected(Text reason)
//        {
//
//        }
//
//        @Override
//        public ClientConnection getConnection()
//        {
//            throw new NotImplementedException("Sorry");
//        }
//    }
}
