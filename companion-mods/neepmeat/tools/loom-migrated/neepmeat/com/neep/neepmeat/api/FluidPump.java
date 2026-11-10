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
import com.neep.neepmeat.transport.api.FluidTransportConstants;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Supplier;

public interface FluidPump
{
    static FluidPump of(float flow, boolean isStorage)
    {
        return new FluidPump()
        {
            @Override
            public float getFlow()
            {
                return getMode().isDriving() ? flow : 0;
            }

            @Override
            public AcceptorModes getMode()
            {
                return AcceptorModes.byFlow(flow);
            }

            @Override
            public boolean isStorage()
            {
                return isStorage;
            }

        };
    }

    static FluidPump of(float flow, Supplier<AcceptorModes> supplier)
    {
        return new FluidPump()
        {
            @Override
            public float getFlow()
            {
                return getMode().isDriving() ? flow : 0;
            }

            @Override
            public AcceptorModes getMode()
            {
                return supplier.get();
            }


            @Override
            public boolean isStorage()
            {
                return true;
            }
        };
    }

    static FluidPump of(float flow, Supplier<AcceptorModes> supplier, float pumpHeightMagnitude)
    {
        return new FluidPump()
        {
            @Override
            public float getFlow()
            {
                return getMode().isDriving() ? flow : 0;
            }

            @Override
            public AcceptorModes getMode()
            {
                return supplier.get();
            }


            @Override
            public boolean isStorage()
            {
                return true;
            }

            @Override
            public float getPumpHeight()
            {
                return getMode().getFlow() * pumpHeightMagnitude;
            }
        };
    }

    float getFlow();

    AcceptorModes getMode();

    boolean isStorage();

    default float getPumpHeight()
    {
        return getMode().getFlow() * FluidTransportConstants.DEFAULT_PUMP_HEIGHT;
    }

    BlockApiLookup<FluidPump, Direction> SIDED =
            BlockApiLookup.get(new ResourceLocation(NeepMeat.NAMESPACE, "sided_fluid_pump"), FluidPump.class, Direction.class);

}
