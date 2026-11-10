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

package com.neep.neepmeat.machine.fluid_exciter;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.processing.FluidEnegyRegistry;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.transport.api.BlockEntityUnloadListener;
import com.neep.neepmeat.transport.api.pipe.AbstractBloodAcceptor;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import com.neep.neepmeat.transport.api.pipe.VascularConduitEntity;
import com.neep.neepmeat.transport.api.pipe.VascularConduitEntityProvider;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import com.neep.meatlib.transfer.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.SingleVariantStorage;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class FluidExciterBlockEntity extends SyncableBlockEntity implements BlockEntityUnloadListener, VascularConduitEntityProvider
{
    protected long output;

    protected long injectedOutput;

    protected BlockApiCache<Storage<FluidVariant>, Direction> downCache;

    protected SingleVariantStorage<FluidVariant> inputStorage = new WritableSingleFluidStorage(30 * FluidConstants.BUCKET, this::setChanged)
    {
        protected boolean canInsert(FluidVariant variant)
        {
            return variant.isOf(NMFluids.ETHEREAL_FUEL.still());
        }

        @Override
        public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction)
        {
            if (downCache == null) updateCache();

            if (!(level instanceof ServerLevel serverWorld && serverWorld.getServer().isSameThread())) return 0;

            double baseEnergy1 = FluidEnegyRegistry.getInstance().getOrEmpty(insertedVariant.getFluid()).baseEnergy() * 1.5;
            output = (long) Math.ceil(baseEnergy1 * maxAmount);

            return maxAmount;
        }

        protected boolean canExtract(FluidVariant variant)
        {
            return false;
        }

        public boolean supportsExtraction()
        {
            return false;
        }
    };

    private final BloodAcceptor bloodAcceptor = new AbstractBloodAcceptor()
    {
        public long getOutput()
        {
            // Reset output so that it drops to zero when fluid input stops.
            injectedOutput = output;
            output = 0;
            return injectedOutput;
        }

        public Mode getMode()
        {
            return Mode.SOURCE;
        }
    };

    private final AbstractVascularConduitEntity conduitEntity;

    public FluidExciterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        conduitEntity = new AbstractVascularConduitEntity(this.worldPosition);
    }

    protected void updateCache()
    {
        downCache = BlockApiCache.create(FluidStorage.SIDED, (ServerLevel) level, worldPosition.below());
    }

    public SingleVariantStorage<FluidVariant> getInputStorage(Direction direction)
    {
        return inputStorage;
    }

    public static BloodAcceptor getBloodAcceptorFromTop(Level world, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Direction direction)
    {
        if (world.getBlockEntity(pos.below()) instanceof FluidExciterBlockEntity be)
        {
            return be.bloodAcceptor;
        }
        return null;
    }

    public AbstractVascularConduitEntity getConduitEntity(Void unused)
    {
        return conduitEntity;
    }

    @Override
    public void setRemoved()
    {
        conduitEntity.onRemove();
        super.setRemoved();
    }

    @Override
    public void onUnload(LevelChunk chunk)
    {
        conduitEntity.onUnload();
    }

    @Override
    public VascularConduitEntity get()
    {
        return conduitEntity;
    }

    public long getOutput()
    {
        return injectedOutput;
    }
}
