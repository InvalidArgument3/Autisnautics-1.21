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

package com.neep.neepmeat.transport.api.pipe;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.blood_network.BloodTransferChangeListener;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface BloodAcceptor
{
    BlockApiLookup<BloodAcceptor, Direction> SIDED = BlockApiLookup.get(
            new ResourceLocation(NeepMeat.NAMESPACE, "sided_blood_acceptor"),
            BloodAcceptor.class, Direction.class);


    // TODO: rename to consumeOutput
    default long getOutput()
    {
        return 0;
    }
    default float updateInflux(float influx) { return influx; };

    default void setChangeListener(@Nullable BloodTransferChangeListener network) {};

    Mode getMode();

    enum Mode
    {
        SINK,
        SOURCE,
        ACTIVE_SINK,
        BALANCE;

        public boolean isOut()
        {
            return this == SOURCE;
        }
    }
}
