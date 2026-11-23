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
 * Spout spawning and enlightenment are not in this tick. Air and work fluid follow the upstream amounts.
 */

package com.neep.neepmeat.machine.charnel_pump;

import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.Transaction;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.fluid.CompressedAir;
import com.neep.neepmeat.fluid.WorkFluid;
import com.neep.neepmeat.machine.EffectMachines;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.process.CharnelPumpProcess;
import com.neep.neepmeat.machine.well_head.WellHeadBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class CharnelPumpBlockEntity extends BlockEntity implements LivingMachineComponent {
    public boolean hasAir;
    public float animationTicks;
    private float progress;
    private float progressIncrement;

    public float progressIncrement() {
        return progressIncrement;
    }

    public CharnelPumpBlockEntity(BlockPos pos, BlockState state) {
        super(EffectMachines.CHARNEL_PUMP_BE.get(), pos, state);
    }

    public void serverTick(double puPower, Storage<FluidVariant> inputStorage, Transaction transaction) {
        List<WellHeadBlockEntity> wells = findWells();
        boolean air = false;
        try (Transaction inner = transaction.openNested()) {
            long droplets = Math.max(10L, (long) (puPower * CharnelPumpProcess.POWER_TO_AIR));
            if (inputStorage.extract(FluidVariant.of(CompressedAir.STILL.get()), droplets, inner) == droplets) {
                air = true;
                inner.commit();
            }
        }
        hasAir = air;
        long bottle = CharnelPumpProcess.DISTRIBUTE_AMOUNT;
        boolean fluidAvailable = simulate(inputStorage, FluidVariant.of(WorkFluid.STILL.get()), bottle, transaction) == bottle;
        if (CharnelPumpProcess.canRun(puPower) && hasAir && fluidAvailable) {
            progressIncrement = (float) Math.min(1.9, puPower);
            progress = (float) Math.max(0.0, progress - puPower);
            animationTicks = progress;
            if (progress <= 0f) {
                progress = 100f;
                for (WellHeadBlockEntity well : wells) {
                    try (Transaction inner = transaction.openNested()) {
                        if (inputStorage.extract(FluidVariant.of(WorkFluid.STILL.get()), bottle, inner) == bottle) {
                            well.receiveFluid(bottle, inner);
                            inner.commit();
                        }
                    }
                    break;
                }
            }
        }
    }

    private static long simulate(Storage<FluidVariant> storage, FluidVariant variant, long amount, Transaction transaction) {
        try (Transaction inner = transaction.openNested()) {
            long extracted = storage.extract(variant, amount, inner);
            inner.abort();
            return extracted;
        }
    }

    private List<WellHeadBlockEntity> findWells() {
        List<WellHeadBlockEntity> found = new ArrayList<>();
        if (level == null) {
            return found;
        }
        BlockPos.betweenClosed(worldPosition.offset(-8, -4, -8), worldPosition.offset(8, 4, 8)).forEach(pos -> {
            if (level.getBlockEntity(pos) instanceof WellHeadBlockEntity well) {
                found.add(well);
            }
        });
        return found;
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType() {
        return LivingMachineComponents.CHARNEL_PUMP;
    }
}
