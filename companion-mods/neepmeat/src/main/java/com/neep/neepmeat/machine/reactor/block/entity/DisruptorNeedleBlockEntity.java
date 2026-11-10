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

package com.neep.neepmeat.machine.reactor.block.entity;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.machine.EffectMachines;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.reactor.disruptor.DisruptorNeedle;
import com.neep.neepmeat.machine.reactor.disruptor.DisruptorNeedleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DisruptorNeedleBlockEntity extends BlockEntity implements DisruptorNeedle {
    public DisruptorNeedleBlockEntity(BlockPos pos, BlockState state) {
        super(EffectMachines.DISRUPTOR_NEEDLE_BE.get(), pos, state);
    }

    @Override
    public @Nullable ReactionCoreBlockEntity findCore() {
        if (level == null) {
            return null;
        }
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof ReactionCoreBlockEntity found) {
                return found;
            }
        }
        return null;
    }

    @Override
    public void updateState(boolean active) {
        if (level != null && getBlockState().getValue(DisruptorNeedleBlock.ACTIVE) != active) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(DisruptorNeedleBlock.ACTIVE, active));
        }
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends com.neep.neepmeat.api.live_machine.LivingMachineComponent> getComponentType() {
        return LivingMachineComponents.DISRUPTOR_NEEDLE;
    }
}
