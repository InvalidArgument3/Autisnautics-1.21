/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.live_machine;

import java.lang.reflect.Array;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ComponentMap {
    @SuppressWarnings("unchecked")
    public final Set<LivingMachineComponent>[] componentMap =
            (Set<LivingMachineComponent>[]) Array.newInstance(Set.class, ComponentType.Simple.size());
    public final BitSet bits = new BitSet();

    public void add(LivingMachineComponent component) {
        int idx = component.componentType().getBitIdx();
        if (componentMap[idx] == null) {
            componentMap[idx] = new HashSet<>();
        }
        bits.set(idx);
        componentMap[idx].add(component);
    }

    public int length() {
        return bits.length();
    }

    public int nextSetBit(int index) {
        return bits.nextSetBit(index);
    }

    @SuppressWarnings("unchecked")
    public <T> Collection<T> get(int idx) {
        if (!bits.get(idx) || componentMap[idx] == null) {
            return Collections.emptySet();
        }
        return (Collection<T>) componentMap[idx];
    }

    public boolean has(int index) {
        return bits.get(index);
    }

    public void clear(int idx, Consumer<LivingMachineComponent> handler) {
        Set<LivingMachineComponent> set = componentMap[idx];
        if (set != null) {
            set.forEach(handler);
            set.clear();
            bits.clear(idx);
        }
    }
}
