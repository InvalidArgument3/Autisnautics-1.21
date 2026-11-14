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
 * Random ore ejection is not generated. The received work-fluid amount is kept so the pump's transfer can be seen.
 */

package com.neep.neepmeat.machine.well_head;

import com.neep.meatlib.transfer.TransactionContext;
import com.neep.neepmeat.machine.EffectMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WellHeadBlockEntity extends BlockEntity {
    private long received;

    public WellHeadBlockEntity(BlockPos pos, BlockState state) {
        super(EffectMachines.WELL_HEAD_BE.get(), pos, state);
    }

    public void receiveFluid(long amount, TransactionContext transaction) {
        received += amount;
        if (transaction instanceof com.neep.meatlib.transfer.Transaction open) {
            long taken = amount;
            open.snapshot(() -> received -= taken);
        }
    }

    public long received() {
        return received;
    }
}
