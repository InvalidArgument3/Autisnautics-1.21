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
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.EffectMachines;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DisruptorPortBlockEntity extends BlockEntity implements LivingMachineComponent {
    private int disruption;
    @Nullable
    private ReactionCoreBlockEntity core;

    public DisruptorPortBlockEntity(BlockPos pos, BlockState state) {
        super(EffectMachines.DISRUPTOR_PORT_BE.get(), pos, state);
    }

    /** NeepBus "Disruptor Power" write. */
    public void setDisruption(int disruption) {
        this.disruption = disruption;
    }

    public int getDisruption() {
        return disruption;
    }

    public void setCore(@Nullable ReactionCoreBlockEntity core) {
        this.core = core;
    }

    @Nullable
    public ReactionCoreBlockEntity getCore() {
        return core;
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType() {
        return LivingMachineComponents.DISRUPTOR_PORT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("disruption", disruption);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        disruption = tag.getInt("disruption");
    }
}
