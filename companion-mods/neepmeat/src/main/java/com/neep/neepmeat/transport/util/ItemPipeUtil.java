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

package com.neep.neepmeat.transport.util;

import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Upstream ejects into an item pipe. Pipes are not ported, so the operation moves nothing
 * and the output port keeps the stack.
 */
public final class ItemPipeUtil {
    private ItemPipeUtil() {}

    public static final class EjectOperation {
        public static EjectOperation of(Level level, BlockPos pos, Direction facing) {
            return new EjectOperation();
        }

        public EjectOperation useRouting(boolean routing) {
            return this;
        }

        public EjectOperation stackToAny(ItemVariant resource, long amount, TransactionContext transaction) {
            return this;
        }

        public long amount() {
            return 0;
        }
    }
}
