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

import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;

import java.util.function.Supplier;

public interface FluidPump {
    float DEFAULT_PUMP_HEIGHT = 1.0F;

    static FluidPump of(float flow, Supplier<AcceptorModes> supplier) {
        return new FluidPump() {
            @Override
            public float getFlow() {
                return getMode().isDriving() ? flow : 0;
            }

            @Override
            public AcceptorModes getMode() {
                return supplier.get();
            }

            @Override
            public boolean isStorage() {
                return true;
            }
        };
    }

    float getFlow();

    AcceptorModes getMode();

    boolean isStorage();

    default float getPumpHeight() {
        return getMode().getFlow() * DEFAULT_PUMP_HEIGHT;
    }
}
