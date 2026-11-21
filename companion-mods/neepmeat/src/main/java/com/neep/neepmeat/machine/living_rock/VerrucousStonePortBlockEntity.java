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

package com.neep.neepmeat.machine.living_rock;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.FluidPump;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import com.neep.neepmeat.transport.api.AutoEjector;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class VerrucousStonePortBlockEntity extends SyncableBlockEntity
{
    private final WritableSingleFluidStorage storage = new WritableSingleFluidStorage(FluidConstants.BUCKET * 4)
    {
        @Override
        protected boolean canInsert(FluidVariant variant)
        {
            return variant.isOf(com.neep.neepmeat.fluid.FluidForms.still(NMFluids.ETHEREAL_FUEL));
        }
    };
    private final FluidPump pump = FluidPump.of(-1, () -> AcceptorModes.PUSH);

    private final AutoEjector<FluidVariant> ejector;

    private int structures = 1;
    private int ports = 1;

    public VerrucousStonePortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.ejector = addListener(AutoEjector.simpleFluid(() -> state.getValue(VerrucousStonePortBlock.FACING), this, storage, pump));
    }

    public void serverTick()
    {
        if (level.getGameTime() % 80 == 0)
            updateStructure(worldPosition);

        createFluid();

        ejector.tick(null);
    }

    private void createFluid()
    {
        try (Transaction transaction = Transaction.openOuter())
        {
            int passivePower = NeepMeat.CONFIG.verrucousStone.passiveCoefficient.getRealValue() * structures
                    / Math.max(1, ports); // Don't think it can ever be zero, but I want to be sure.

            long passiveAmount = PowerUtil.absToAmount(com.neep.neepmeat.fluid.FluidForms.still(NMFluids.ETHEREAL_FUEL), passivePower);
            if (passiveAmount > 0)
            {
                storage.insert(FluidVariant.of(com.neep.neepmeat.fluid.FluidForms.still(NMFluids.ETHEREAL_FUEL)), passiveAmount, transaction);
                transaction.commit();
            }
        }
    }

    private void updateStructure(BlockPos origin)
    {
        structures = 1;
        ports = 1;

        Queue<BlockPos> posQueue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        posQueue.add(origin);
        visited.add(origin);

        while (!posQueue.isEmpty())
        {
            BlockPos current = posQueue.poll();

            for (Direction direction : Direction.values())
            {
                BlockPos offset = current.relative(direction);

                if (visited.contains(offset))
                    continue;

                visited.add(offset);

                BlockState offsetState = level.getBlockState(offset);
                Block offsetBlock = offsetState.getBlock();

                if (offsetBlock instanceof VerrucousStoneBlock)
                {
                    structures++;
                    posQueue.add(offset);
                }
                else if (offsetBlock instanceof VerrucousStonePortBlock)
                {
                    ports++;
                    structures++;
                    posQueue.add(offset);
                }
            }
        }
    }

    @Nullable
    public Storage<FluidVariant> getStorage(@Nullable Direction direction)
    {
        Direction facing = getBlockState().getValue(VerrucousStonePortBlock.FACING);

        if (direction == null || direction == facing)
            return storage;

        return null;
    }

    @Nullable
    public FluidPump getPump(@Nullable Direction direction)
    {
        Direction facing = getBlockState().getValue(VerrucousStonePortBlock.FACING);

        if (direction == null || direction == facing)
            return pump;

        return null;
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        storage.writeNbt(nbt);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        storage.readNbt(nbt);
    }
}
