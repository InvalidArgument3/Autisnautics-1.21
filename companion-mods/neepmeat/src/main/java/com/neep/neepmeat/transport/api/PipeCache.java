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

package com.neep.neepmeat.transport.api;

import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface PipeCache
{
    boolean retrieve(BlockPos to, Direction in, ItemVariant variant, long amount, TransactionContext transaction);

    long eject(BlockPos from, Direction out, ItemVariant variant, long amount, TransactionContext transaction);

    long route(BlockPos from, Direction in, BlockPos to, Direction out, ItemVariant variant, long amount, TransactionContext transaction);

    long route(BlockPos from, Direction fromFace, com.neep.neepmeat.transport.item_network.ItemRoute route, ItemVariant variant, long amount, TransactionContext transaction);
}
