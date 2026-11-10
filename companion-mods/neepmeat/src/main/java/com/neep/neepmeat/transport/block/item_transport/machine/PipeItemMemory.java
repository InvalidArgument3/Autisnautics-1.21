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

package com.neep.neepmeat.transport.block.item_transport.machine;

import com.neep.neepmeat.transport.item_network.ItemInPipe;
import com.neep.meatlib.transfer.ItemVariant;

import java.util.HashMap;
import java.util.Map;

// Oh gawd
public class PipeItemMemory
{
    private final Map<ItemVariant, Integer> map = new HashMap<>();

    public void tickOff(ItemVariant variant, int amount)
    {
        if (amount > 0)
            map.computeIfPresent(variant, (k, v) -> Math.max(0, v - amount));
    }

    public int get(ItemVariant variant)
    {
        return map.getOrDefault(variant, 0);
    }

    public void register(ItemInPipe item)
    {
        map.compute(item.resource(), (k, v) -> Math.toIntExact(v != null ? v + item.amount() : item.amount()));
        item.callback = this;
    }
}
