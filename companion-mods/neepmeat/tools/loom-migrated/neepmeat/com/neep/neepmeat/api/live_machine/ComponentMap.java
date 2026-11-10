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

package com.neep.neepmeat.api.live_machine;

import com.neep.neepmeat.NeepMeat;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;

public class ComponentMap
{
    public final Set<LivingMachineComponent>[] componentMap = (Set<LivingMachineComponent>[]) Array.newInstance(Set.class, ComponentType.Simple.NEXT_ID);
    public final BitSet bits = new BitSet(); // Active components marked in one-hot codes

    public ComponentMap()
    {

    }

    protected void add(LivingMachineComponent component)
    {
        int idx = component.getComponentType().getBitIdx();

        if (componentMap[idx] == null)
            componentMap[idx] = new HashSet<>();

        bits.set(idx);
        componentMap[idx].add(component);
    }

    public int length()
    {
        return bits.length();
    }

    public int nextSetBit(int i)
    {
        return bits.nextSetBit(i);
    }

    public <T> Collection<T> get(int idx)
    {
        if (!bits.get(idx))
            return Collections.emptySet();

        return (Collection<T>) componentMap[idx];
    }


    public void onRemove(LivingMachineComponent component)
    {
        int idx = component.getComponentType().getBitIdx();
        if (!bits.get(idx))
            return;

        if (componentMap[idx] == null || componentMap[idx].isEmpty())
            bits.clear(idx);
    }

    public void clear(int idx, Consumer<LivingMachineComponent> handler)
    {
        var set = componentMap[idx];
        if (set == null)
        {
            NeepMeat.LOGGER.error("Something has gone horribly wrong");
        }
        else
        {
//            componentMap[idx].forEach(c -> c.setController(null));
            componentMap[idx].forEach(handler);
            componentMap[idx].clear();
        }
    }

    public boolean has(int i)
    {
        return bits.get(i);
    }
}
