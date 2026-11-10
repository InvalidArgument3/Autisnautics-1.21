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

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.util.LazySupplier;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.network.TankMessagePacket;
import com.neep.neepmeat.transport.block.fluid_transport.TankBlock;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("UnstableApiUsage")
public class TankBlockEntity extends SyncableBlockEntity
{
    protected class InternalBuffer extends WritableSingleFluidStorage
    {
        public InternalBuffer(long capacity, Runnable finalCallback)
        {
            super(capacity, finalCallback);
        }

        @Override
        public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction)
        {
            TankBlockEntity upStorage = upCache.get().find(getType());
            long upExtracted = 0;
            if (upStorage != null)
            {
                upExtracted = upStorage.getStorage(null).extract(extractedVariant, maxAmount, transaction);
            }

            if (upExtracted < maxAmount)
            {
                long extracted = super.extract(extractedVariant, maxAmount - upExtracted, transaction);
                return upExtracted + extracted;
            }
            else
            {
                return upExtracted;
            }
        }

        @Override
        public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction)
        {
            var downStorage = downCache.get().find(getType());

            long downInserted = 0;
            if (downStorage != null)
            {
                downInserted = downStorage.getStorage(null).insert(insertedVariant, maxAmount, transaction);
            }

            long inserted = 0;
            if (maxAmount - downInserted > 0)
            {
                inserted = super.insert(insertedVariant, maxAmount - downInserted, transaction);
            }

            return downInserted + inserted;
        }
    };

    protected final InternalBuffer buffer;

    protected LazySupplier<BlockApiCache<TankBlockEntity, BlockEntityType<?>>> downCache = LazySupplier.of(() ->
            BlockApiCache.create(LOOKUP, (ServerLevel) getLevel(), getBlockPos().below()));

    protected LazySupplier<BlockApiCache<TankBlockEntity, BlockEntityType<?>>> upCache = LazySupplier.of(() ->
            BlockApiCache.create(LOOKUP, (ServerLevel) getLevel(), getBlockPos().above()));

    public TankBlockEntity(BlockEntityType type, BlockPos pos, BlockState state, TankType tankType)
    {
        super(type, pos, state);
        buffer = new InternalBuffer(tankType.capacity(), this::sync);
    }

    public WritableSingleFluidStorage getStorage(Direction direction)
    {
        return buffer;
    }

    @Override
    public void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        buffer.toNbt(tag);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        buffer.readNbt(tag);
    }

    @Override
    public void saveToItem(ItemStack stack)
    {
        super.saveToItem(stack);
    }

    public boolean onUse(Player player, InteractionHand hand)
    {
        if (WritableSingleFluidStorage.handleInteract(buffer, level, player, hand))
        {
            return true;
        }
        else if (!level.isClientSide())
        {
            showContents((ServerPlayer) player, level, getBlockPos(), buffer);
            return true;
        }
        return true;
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
    }

    public static void showContents(ServerPlayer player, Level world, BlockPos pos, StorageView<FluidVariant> buffer)
    {
        world.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1f, 1.5f);
        TankMessagePacket.send(player, pos, buffer.getAmount(), buffer.getResource());
    }

    public static final BlockApiLookup<TankBlockEntity, BlockEntityType<?>> LOOKUP = BlockApiLookup.get(
            new ResourceLocation(NeepMeat.NAMESPACE, "tank_lookup"),
            TankBlockEntity.class, (Class<BlockEntityType<?>>) (Class<?>) BlockEntityType.class
    );

    public static <T extends BlockEntity> TankBlockEntity find(T be, BlockEntityType<?> type)
    {
        if (be.getType().equals(type) && be.getBlockState().getValue(TankBlock.AXIS).isVertical())
            return (TankBlockEntity) be;

        return null;
    }
}
