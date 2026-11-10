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

package com.neep.neepmeat.machine.transducer;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.util.LazySupplier;
import com.neep.neepmeat.api.Burner;
import com.neep.neepmeat.api.FluidPump;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import com.neep.neepmeat.transport.api.AutoEjector;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

@SuppressWarnings("UnstableApiUsage")
public class TransducerBlockEntity extends SyncableBlockEntity
{
    protected WritableSingleFluidStorage storage = new WritableSingleFluidStorage(FluidConstants.BUCKET, this::setChanged)
    {
        @Override
        public boolean supportsInsertion()
        {
            return false;
        }
    };

    protected LazySupplier<BlockApiCache<Burner, Direction>> burnerCache = LazySupplier.of(() ->
            BlockApiCache.create(Burner.LOOKUP, (ServerLevel) this.getLevel(), worldPosition.below(2)));

    protected BlockApiCache<Storage<FluidVariant>, Direction> inputCache;

    protected FluidPump pump = FluidPump.of(-0.5f, () -> AcceptorModes.PUSH);

    protected long outputPower;
    protected boolean needsUpdate = true;

    private final AutoEjector<FluidVariant> ejector = addListener(AutoEjector.simpleFluid(() -> Direction.UP, this, storage, pump));

    public TransducerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public TransducerBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.TRANSDUCER, pos, state);
    }

    public SingleVariantStorage<FluidVariant> getStorage(Direction ignored)
    {
        return storage;
    }

    public FluidPump getPump(Direction ignored)
    {
        return pump;
    }

    public void updateBurners()
    {
        inputCache = BlockApiCache.create(FluidStorage.SIDED, (ServerLevel) level, worldPosition.below());
        needsUpdate = false;
    }

    public void tick()
    {
        if (needsUpdate)
        {
            updateBurners();
        }

        outputPower = 0;

        Burner burner = burnerCache.get().find(Direction.UP);
        if (burner != null)
        {
            burner.tickPowerConsumption();
            outputPower += (long) burner.getOutputPower();
        }

        Storage<FluidVariant> inputStorage = inputCache.find(Direction.UP);
        if (inputStorage != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                // Calculate amount of fluid to be produced from current power output
                long produceAmount = PowerUtil.absToAmount(NMFluids.ETHEREAL_FUEL.still(), outputPower);

                // Extract from water tank
                FluidVariant inputVariant = StorageUtil.findStoredResource(inputStorage, v -> v.isOf(Fluids.WATER));
                if (inputVariant != null)
                {
                    long extracted = inputStorage.extract(inputVariant, produceAmount, transaction);

                    // Insert output fluid into internal buffer
                    long inserted = storage.insert(FluidVariant.of(NMFluids.ETHEREAL_FUEL.still()), Math.min(produceAmount, extracted), transaction);

                    if (inserted == produceAmount)
                    {
                        transaction.commit();
                    }
                    else
                    {
                        transaction.abort();
                    }
                }
            }
        }

        ejector.tick(null);
    }

    public void update()
    {
        this.needsUpdate = true;
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, TransducerBlockEntity be)
    {
        be.tick();
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        storage.toNbt(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        storage.readNbt(nbt);
    }
}
