/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.live_machine;

import java.util.ArrayList;
import java.util.List;

public interface ComponentType<T extends LivingMachineComponent> {
    List<ComponentType<?>> ID_TO_TYPE = new ArrayList<>();

    int getBitIdx();

    class Simple<T extends LivingMachineComponent> implements ComponentType<T> {
        private static int nextId;

        private final int id = nextId++;

        public static int size() {
            return nextId;
        }

        @Override
        public int getBitIdx() {
            return id;
        }
    }
}
