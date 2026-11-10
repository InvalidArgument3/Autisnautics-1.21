/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package com.neep.meatlib.event;

import java.util.ArrayList;
import java.util.List;

/** Fabric {@code Event.register(listener)}. Listeners stay as lambdas. */
public final class RegisterEvent<T> {
    private final List<T> listeners = new ArrayList<>();

    public void register(T listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public List<T> listeners() {
        return List.copyOf(listeners);
    }
}
