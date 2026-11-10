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

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.block.IntegrationPortBlock;
import com.neep.neepmeat.machine.live_machine.component.PoweredComponent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class IntegrationPortBlockEntity extends SyncableBlockEntity implements LivingMachineComponent, PoweredComponent
{
    private final WritableSingleFluidStorage fluidStorage = new WritableSingleFluidStorage(FluidConstants.INGOT, this::setChanged)
    {
        @Override
        protected boolean canInsert(FluidVariant variant)
        {
            return getBlockState().getValue(IntegrationPortBlock.OPEN)
                    && super.canInsert(variant)
                    && variant.isOf(NMFluids.WORK_FLUID.still());
        }
    };

    public IntegrationPortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        fluidStorage.writeNbt(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        fluidStorage.readNbt(nbt);
    }

    public WritableSingleFluidStorage getFluidStorage(Direction unused)
    {
        return fluidStorage;
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType()
    {
        return LivingMachineComponents.INTEGRATION_PORT;
    }

    @Override
    public float progressIncrement()
    {
        return 0;
    }

    @Override
    public void setProgressIncrement(float progressIncrement)
    {
        boolean prevOpen = getBlockState().getValue(IntegrationPortBlock.OPEN);
        boolean nowOpen = progressIncrement == 0;

        if (prevOpen != nowOpen)
        {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(IntegrationPortBlock.OPEN, nowOpen));
            if (nowOpen)
            {
                level.playSound(null, worldPosition, SoundEvents.NETHER_WOOD_DOOR_OPEN, SoundSource.BLOCKS, 1, 1);
                level.playSound(null, worldPosition, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1, 1);
            }
            else
            {
                level.playSound(null, worldPosition, SoundEvents.NETHER_WOOD_DOOR_CLOSE, SoundSource.BLOCKS, 1, 1);
                level.playSound(null, worldPosition, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1, 1);
            }
        }
    }

    public boolean apply(Player player, InteractionHand hand, ItemStack stack)
    {
        if (getBlockState().getValue(IntegrationPortBlock.OPEN))
        {
            if (!level.isClientSide())
            {
                try (Transaction transaction = Transaction.openOuter())
                {
                    long inserted = fluidStorage.insert(NMFluids.WORK_FLUID.variant(), FluidConstants.INGOT, transaction);
                    if (inserted == FluidConstants.INGOT)
                    {
                        transaction.commit();
                        level.playSound(null, worldPosition, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1, 1);
                        stack.shrink(1);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
