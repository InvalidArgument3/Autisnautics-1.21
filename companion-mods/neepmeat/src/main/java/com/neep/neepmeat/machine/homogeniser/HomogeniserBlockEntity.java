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

package com.neep.neepmeat.machine.homogeniser;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageUtil;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class HomogeniserBlockEntity extends SyncableBlockEntity implements MotorisedBlock, MotorisedBlock.DiagnosticsProvider
{
    protected HomogeniserStorage storage = new HomogeniserStorage(this);
    protected State state = State.IDLE;

    protected final float maxProgress = 20;
    protected float progress = 0;
    protected float power = 0;

    public HomogeniserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        storage.writeNbt(nbt);
        nbt.putInt("state", state.ordinal());
        nbt.putFloat("progress", progress);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        storage.readNbt(nbt);
        this.state = State.values()[nbt.getInt("state")];
        this.progress = nbt.getFloat("progress");
    }

    public WritableStackStorage getItemStorage(@Nullable Direction direction)
    {
        return storage.itemStorage;
    }

    public HomogeniserStorage getStorage()
    {
        return storage;
    }

    protected boolean canProcess(TransactionContext transaction)
    {
        var inputItemStorage = storage.itemStorage;

        if (!inputItemStorage.isEmpty())
        {
            var entry = FoodAdditives.getInstance().get(inputItemStorage.getResource().getItem());
            if (entry != null)
            {
                try (Transaction test = transaction.openNested())
                {
                    Storage<FluidVariant> inputFluidStorage = storage.getInputFluidStorage();

                    ResourceAmount<FluidVariant> inputVariant =
                            StorageUtil.findExtractableContent(inputFluidStorage, v -> v.isOf(com.neep.neepmeat.fluid.FluidForms.still(NMFluids.FOOD)), test);

                    if (inputVariant != null
                            && entry.canApply(inputVariant.resource())
                            && inputVariant.amount() >= entry.getAmount())
                    {

                        test.abort();
                        return true;
                    }

                    test.abort();
                }
            }
        }
        return false;
    }

    protected void ejectOutput(TransactionContext transaction)
    {
        try (Transaction inner = transaction.openNested())
        {
            var inputItemStorage = storage.itemStorage;
            if (!inputItemStorage.isEmpty())
            {
                var entry = FoodAdditives.getInstance().get(inputItemStorage.getResource().getItem());
                if (entry != null)
                {
                    Storage<FluidVariant> inputFluidStorage = storage.getInputFluidStorage();
                    Storage<FluidVariant> outputFluidStorage = storage.getOutputStorage();

                    ResourceAmount<FluidVariant> inputVariant =
                            StorageUtil.findExtractableContent(inputFluidStorage, v -> v.isOf(com.neep.neepmeat.fluid.FluidForms.still(NMFluids.FOOD)), inner);

                    if (inputVariant != null
                            && entry.canApply(inputVariant.resource())
                            && inputVariant.amount() >= entry.getAmount())
                    {

                        long extracted = inputFluidStorage.extract(inputVariant.resource(), entry.getAmount(), inner);

                        FluidVariant outputVariant = entry.apply(inputVariant.resource());

                        long inserted = outputFluidStorage.insert(outputVariant, extracted, inner);

                        if (inserted == extracted)
                        {
                            inner.commit();
                            return;
                        }
                    }

                    inner.abort();
                }
            }
        }
    }

    @Override
    public boolean motorTick(MotorEntity motor)
    {
        try (Transaction transaction = Transaction.openOuter())
        {
            switch (state)
            {
                case IDLE ->
                {
                    if (level.getGameTime() % 2 == 0 && canProcess(transaction))
                    {
                        progress = 0;

                        state = State.PROCESSING;
                        sync();
                    }
                }

                case PROCESSING ->
                {
                    progress = Math.min(maxProgress, progress + power);

                    if (progress == maxProgress)
                    {
                        progress = 0;

                        ejectOutput(transaction);
                        state = State.IDLE;
                        sync();
                    }
                }
            }
            transaction.commit();
        }
        return true;
    }

    float angle = 0;

    public void clientTick()
    {
        angle += 1.1;

        if (state == State.PROCESSING && !storage.itemStorage.isEmpty())
        {
            double radius = 0.2;

            double cx = getBlockPos().getX() + 0.5;
            double cy = getBlockPos().getY() + 1;
            double cz = getBlockPos().getZ() + 0.5;

            double px = cx + Math.sin(angle) * radius;
            double pz = cz + Math.cos(angle) * radius;

            double vx = (px - cx) * 0.5;
            double vy = 0.35;
            double vz = (pz - cz) * 0.5;

            level.addParticle(
                    new ItemParticleOption(ParticleTypes.ITEM, storage.itemStorage.getAsStack()),
                    px, cy, pz,
                    vx, vy, vz
            );
        }
    }

    @Override
    public void setInputPower(float power)
    {
        this.power = power;
    }

    @Override
    public float getLoadTorque()
    {
        return MotorisedBlock.super.getLoadTorque();
    }

    @Override
    public Diagnostics getDiagnostics()
    {
        return Diagnostics.insufficientPower(false, power, 0);
    }


    protected enum State
    {
        IDLE,
        PROCESSING
    }
}
