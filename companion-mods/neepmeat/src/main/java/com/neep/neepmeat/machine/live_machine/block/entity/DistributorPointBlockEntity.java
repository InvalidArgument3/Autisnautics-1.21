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
 * The address, queue, and worm animation from upstream are not in this class yet.
 * The mode enum is the one PointHandle stores.
 */

package com.neep.neepmeat.machine.live_machine.block.entity;

public final class DistributorPointBlockEntity {
    private DistributorPointBlockEntity() {}

    public enum Mode {
        SEND,
        RECEIVE,
        SEND_AND_RECEIVE;

        public static Mode fromNbt(int mode) {
            return values()[mode];
        }

        public boolean canReceive() {
            return this == RECEIVE || this == SEND_AND_RECEIVE;
        }
    }
}
