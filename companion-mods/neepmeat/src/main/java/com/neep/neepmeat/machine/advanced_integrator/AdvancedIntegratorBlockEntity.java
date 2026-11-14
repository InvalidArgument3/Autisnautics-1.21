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
 *
 * Passive accumulation adds four data every four ticks until eight gieb. The client flex animation is not ported.
 */

package com.neep.neepmeat.machine.advanced_integrator;

import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import com.neep.neepmeat.api.DataResource;
import com.neep.neepmeat.api.data.DataUtil;
import com.neep.neepmeat.machine.StageMachines;
import com.neep.neepmeat.machine.integrator.Integrator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedIntegratorBlockEntity extends BlockEntity implements Integrator {
    public static final long PASSIVE_LIMIT = 8 * DataUtil.GIEB;
    public static final long CAPACITY = 16 * DataUtil.GIEB;

    private long data;
    private int ticks;
    private BlockPos lookTarget = BlockPos.ZERO;

    public AdvancedIntegratorBlockEntity(BlockPos pos, BlockState state) {
        super(StageMachines.ADVANCED_INTEGRATOR_BE.get(), pos, state);
    }

    public void serverTick() {
        ticks++;
        if (data < PASSIVE_LIMIT && ticks % 4 == 0) {
            try (Transaction transaction = Transaction.openOuter()) {
                insert(DataResource.NORMAL, 4, transaction);
                transaction.commit();
            }
        }
    }

    public long insert(DataResource variant, long amount, TransactionContext transaction) {
        if (variant.isBlank() || amount <= 0) {
            return 0;
        }
        long inserted = Math.min(amount, CAPACITY - data);
        if (inserted > 0) {
            data += inserted;
            setChanged();
        }
        return inserted;
    }

    public long data() {
        return data;
    }

    @Override
    public boolean canEnlighten() {
        return data > 0;
    }

    @Override
    public void setLookPos(BlockPos pos) {
        this.lookTarget = pos;
    }

    @Override
    public void spawnBeam(Level world, BlockPos pos) {
        setLookPos(pos);
    }

    @Override
    public long getData(DataResource variant) {
        return data;
    }

    @Override
    public float extract(DataResource variant, long amount, TransactionContext transaction) {
        long extracted = Math.min(amount, data);
        if (extracted > 0) {
            data -= extracted;
            setChanged();
            return extracted;
        }
        return 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("amount", data);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        data = tag.getLong("amount");
    }
}
