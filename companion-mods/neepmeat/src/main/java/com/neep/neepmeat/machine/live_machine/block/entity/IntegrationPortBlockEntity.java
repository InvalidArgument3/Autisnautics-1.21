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

import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.Transaction;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.fluid.WorkFluid;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.block.IntegrationPortBlock;
import com.neep.neepmeat.machine.live_machine.component.PoweredComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class IntegrationPortBlockEntity extends BlockEntity implements LivingMachineComponent, PoweredComponent {
    private final WritableSingleFluidStorage storage = new WritableSingleFluidStorage(FluidConstants.INGOT, this::setChanged) {
        @Override
        public long insert(FluidVariant resource, long maxAmount, com.neep.meatlib.transfer.TransactionContext transaction) {
            if (level != null && !getBlockState().getValue(IntegrationPortBlock.OPEN)) {
                return 0;
            }
            if (!resource.isOf(WorkFluid.STILL.get())) {
                return 0;
            }
            return super.insert(resource, maxAmount, transaction);
        }
    };

    public IntegrationPortBlockEntity(BlockPos pos, BlockState state) {
        super(LivingMachines.INTEGRATION_BE.get(), pos, state);
    }

    public Storage<FluidVariant> getFluidStorage(Direction side) {
        return storage;
    }

    public boolean apply(Player player, InteractionHand hand, ItemStack stack) {
        if (level == null || !getBlockState().getValue(IntegrationPortBlock.OPEN) || level.isClientSide()) {
            return false;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert(FluidVariant.of(WorkFluid.STILL.get()), FluidConstants.INGOT, transaction);
            if (inserted == FluidConstants.INGOT) {
                transaction.commit();
                level.playSound(null, worldPosition, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1, 1);
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    @Override
    public float progressIncrement() {
        return getBlockState().getValue(IntegrationPortBlock.OPEN) ? 0 : 1;
    }

    @Override
    public void setProgressIncrement(float progressIncrement) {
        if (level == null) {
            return;
        }
        boolean open = progressIncrement == 0;
        if (getBlockState().getValue(IntegrationPortBlock.OPEN) != open) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(IntegrationPortBlock.OPEN, open));
            level.playSound(null, worldPosition, open ? SoundEvents.NETHER_WOOD_DOOR_OPEN : SoundEvents.NETHER_WOOD_DOOR_CLOSE, SoundSource.BLOCKS, 1, 1);
        }
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType() {
        return LivingMachineComponents.INTEGRATION_PORT;
    }
}
