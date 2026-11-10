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

package com.neep.neepbus.util;

import com.neep.neepbus.util.message.DataVariant;

public interface WritePort extends NeepBusPort
{
    void write(DataVariant data);

    /**
     * Anything that remembers a {@link WritePort} must register a listener here.
     * This allows ports to change and swap their addresses and notify caching senders without an expensive BFS.
     */
    @Deprecated
    default void addInvalidateListener(Runnable invalidate) {}
}
