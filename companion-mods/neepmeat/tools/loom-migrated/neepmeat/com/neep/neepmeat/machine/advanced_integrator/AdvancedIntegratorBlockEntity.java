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

package com.neep.neepmeat.machine.advanced_integrator;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.util.ClientComponents;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.DataResource;
import com.neep.neepmeat.api.data.DataUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedIntegratorBlockEntity extends SyncableBlockEntity
{
    private final DataStorage storage = new DataStorage(this::sync);
    private final ClientComponents.Holder<AdvancedIntegratorBlockEntity> holder = new ClientComponents.BlockEntityHolder<>(this);

    public AdvancedIntegratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public void onUse(Player player)
    {
        player.displayClientMessage(Component.translatable("message." + NeepMeat.NAMESPACE + ".integrator.data",
                DataUtil.formatData(storage.getAmount()),
                DataUtil.formatData(storage.getCapacity())), true);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        storage.writeNbt(nbt);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        storage.readNbt(nbt);
        super.load(nbt);
    }

    public DataStorage getDataStorage()
    {
        return storage;
    }

    public void serverTick()
    {
        // Passive accumulation. Exceeding this limit requires a Pylon.
        if (storage.getAmount() < 8 * DataUtil.GIEB && level.getGameTime() % 4 == 0)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                storage.insert(DataResource.NORMAL, 4, transaction);
                transaction.commit();
            }
        }
    }

    public void clientTick()
    {
        holder.get().clientTick();
    }

    public static class DataStorage extends SingleVariantStorage<DataResource>
    {
        private final Runnable finalCallback;

        public DataStorage(Runnable finalCallback)
        {
            this.finalCallback = finalCallback;
        }

        protected void setAmount(long amount)
        {
            this.amount = amount;
        }

        @Override
        protected DataResource getBlankVariant()
        {
            return DataResource.BLANK;
        }

        @Override
        protected long getCapacity(DataResource variant)
        {
            return DataUtil.GIEB * 16;
        }

        @Override
        public long insert(DataResource insertedVariant, long maxAmount, TransactionContext transaction)
        {
            StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);

            if ((insertedVariant.equals(variant) || variant.isBlank()) && canInsert(insertedVariant)) {
                long insertedAmount = Math.min(maxAmount, getCapacity(insertedVariant) - amount);

                if (insertedAmount > 0) {
                    updateSnapshots(transaction);

                    if (variant.isBlank()) {
                        variant = insertedVariant;
                        amount = insertedAmount;
                    } else {
                        amount += insertedAmount;
                    }

                    return insertedAmount;
                }
            }

            return 0;
        }

        public void readNbt(CompoundTag nbt)
        {
            this.variant = DataResource.fromNbt(nbt.getCompound("variant"));
            this.amount = nbt.getLong("amount");
        }

        @Override
        protected void onFinalCommit()
        {
            super.onFinalCommit();
            finalCallback.run();
        }
    }
}
