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

package com.neep.neepmeat.api;

import com.neep.neepmeat.NeepMeat;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface DataPort
{
//    BlockApiLookup<Storage<DataVariant>, Void> DATA_STORAGE =
//            BlockApiLookup.get(new Identifier(NeepMeat.NAMESPACE, "data_storage"),
//                    Storage.asClass(), Void.class);

    BlockApiLookup<DataPort, Void> DATA_PORT =
            BlockApiLookup.get(new ResourceLocation(NeepMeat.NAMESPACE, "data_port"),
                    DataPort.class, Void.class);

    void setTarget(BlockPos pos);

    long receive(DataResource variant, long amount, TransactionContext transaction);
}
