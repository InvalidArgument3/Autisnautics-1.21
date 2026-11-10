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

package com.neep.neepmeat.transport.item_network;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

public class ShittyRoutingCallbackHolder
{
    // Non-persistent list of blocks that are doing routing without a network (such as item pumps).
    // These are block entities, so it's probably good to allow them to be GC'd silently.

    protected Set<WeakReference<ShittyRoutingListener>> listeners = new HashSet<>();

    // Why doesn't this work?
//    protected Set<ShittyRoutingListener> listeners = Collections.newSetFromMap(new WeakHashMap<>());

    public void addListener(ShittyRoutingListener listener)
    {
        listeners.add(new WeakReference<>(listener));
    }

    public void invalidate()
    {
        for (var reference : listeners)
        {
            ShittyRoutingListener object = reference.get();
            if (object != null)
                object.invalidate();
        }

        listeners.clear();
    }
}
