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
import com.neep.neepmeat.api.FluidPump;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.api.FluidTransportConstants;
import com.neep.neepmeat.transport.block.fluid_transport.PumpBlock;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class PumpBlockEntity extends SyncableBlockEntity
{
    // When fluid storage is directly in front, redirect insertions to neighboring storage.

    protected final WritableSingleFluidStorage buffer;

    public static final String FRONT_MODE = "front_mode";
    public static final String BACK_MODE = "back_mode";

    public AcceptorModes frontMode = AcceptorModes.NONE;
    public AcceptorModes backMode = AcceptorModes.NONE;

    protected FluidPump frontPump = FluidPump.of(-0.5f, () -> frontMode, FluidTransportConstants.INSTANCE.basicPumpHeight());
    protected FluidPump backPump = FluidPump.of(0.5f, () -> backMode, FluidTransportConstants.INSTANCE.basicPumpHeight());

    protected boolean activeWithoutRedstone = false;

    public PumpBlockEntity(BlockPos pos, BlockState state)
    {
        super(NMBlockEntities.PUMP, pos, state);

        buffer = new WritableSingleFluidStorage(FluidConstants.BLOCK, this::setChanged);
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
    }

    @Override
    public void setLevel(Level world)
    {
        super.setLevel(world);
    }

    public boolean isActive()
    {
        return getBlockState().getValue(PumpBlock.ACTIVE);
    }

    public void updatePowered(boolean powered)
    {
        boolean active = powered != activeWithoutRedstone;
        updateActive(active);
    }

    private void updateActive(boolean active)
    {
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(PumpBlock.ACTIVE, active));

        if (active)
        {
            frontMode = AcceptorModes.PUSH;
            backMode = AcceptorModes.PULL;
        }
        else
        {
            frontMode = AcceptorModes.NONE;
            backMode = AcceptorModes.NONE;
        }

        this.setChanged();
    }

    @Override
    public void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        buffer.toNbt(tag);
        tag.putInt(FRONT_MODE, frontMode.getId());
        tag.putInt(BACK_MODE, backMode.getId());
        tag.putBoolean("hasFrontStorage", hasFrontStorage);
        tag.putBoolean("hasRearStorage", hasRearStorage);
        tag.putBoolean("activeWithoutRedstone", activeWithoutRedstone);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        buffer.readNbt(tag);
        this.frontMode = AcceptorModes.byId(tag.getInt(FRONT_MODE));
        this.backMode = AcceptorModes.byId(tag.getInt(BACK_MODE));
        this.hasFrontStorage = tag.getBoolean("hasFrontStorage");
        this.hasRearStorage = tag.getBoolean("hasRearStorage");
        this.activeWithoutRedstone = tag.getBoolean("activeWithoutRedstone");
    }

    @Nullable
    public SingleVariantStorage<FluidVariant> getBuffer(Direction direction)
    {
        Direction facing = getBlockState().getValue(PumpBlock.FACING);
        if (direction == facing || direction == facing.getOpposite() || direction == null)
            return buffer;
        else
            return null;
    }

    @Override
    public void sync()
    {
        super.sync();
    }

    public FluidPump getPump(Direction direction)
    {
        Direction facing = getBlockState().getValue(PumpBlock.FACING);
        return direction == facing ? frontPump : direction == facing.getOpposite() ? backPump : null;
    }

    BlockApiCache<Storage<FluidVariant>, Direction> frontCache;
    BlockApiCache<Storage<FluidVariant>, Direction> rearCache;
    protected boolean hasFrontStorage;
    protected boolean hasRearStorage;

    public void updateCache()
    {
        Direction facing = getBlockState().getValue(PumpBlock.FACING);
        frontCache = BlockApiCache.create(FluidStorage.SIDED, (ServerLevel) level, worldPosition.relative(facing));
        rearCache = BlockApiCache.create(FluidStorage.SIDED, (ServerLevel) level, worldPosition.relative(facing.getOpposite()));

        Storage<FluidVariant> frontStorage = frontCache.find(facing.getOpposite());
        Storage<FluidVariant> rearStorage = rearCache.find(facing);
        hasFrontStorage = frontStorage != null;
        hasRearStorage = rearStorage != null;
    }

    // I resent making pumps ticked, it undermines the whole point of ticking fluid networks.
    // [2024] Not anymore! Now EVERYTHING is ticking. EVERY BLOCK IS TICKED NOW. NO MORE OPTIMISATION! I JUST WANT THINGS TO WORK!
    public void tick()
    {
        Direction facing = getBlockState().getValue(PumpBlock.FACING);
        // TODO: PLEASE CHANGE
        long transfer = FluidTransportConstants.INSTANCE.maxTransfer();

        // Create caches if they should be present
        if (hasFrontStorage && frontCache == null || hasRearStorage && rearCache == null)
            updateCache();

        if (rearCache != null && frontCache != null)
        {
            Storage<FluidVariant> frontStorage = frontCache.find(facing.getOpposite());
            Storage<FluidVariant> rearStorage = rearCache.find(facing);
            if (rearStorage != null && backMode.isDriving())
            {
                long tr1 = StorageUtil.move(rearStorage, buffer, v -> true, transfer, null);
            }
            if (frontStorage != null && frontMode.isDriving())
            {
                long tr2 = StorageUtil.move(buffer, frontStorage, v -> true, transfer, null);
            }
        }
    }

    public void changeMode()
    {
        activeWithoutRedstone = !activeWithoutRedstone;
        updatePowered(getLevel().hasNeighborSignal(worldPosition));
        setChanged();
    }
}
