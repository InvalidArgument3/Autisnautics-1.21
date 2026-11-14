/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package net.fabricmc.fabric.api.event;

import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Fabric array-backed event. {@code invoker()} runs every registered listener. */
public final class Event<T> {
    public static final ResourceLocation DEFAULT_PHASE = ResourceLocation.fromNamespaceAndPath("fabric", "default");

    private final Class<T> type;
    private final List<T> listeners = new ArrayList<>();
    private final Function<T[], T> factory;

    public Event(Class<T> type, Function<T[], T> factory) {
        this.type = type;
        this.factory = factory;
    }

    public void register(T listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public List<T> listeners() {
        return List.copyOf(listeners);
    }

    @SuppressWarnings("unchecked")
    public T invoker() {
        T[] array = (T[]) Array.newInstance(type, listeners.size());
        return factory.apply(listeners.toArray(array));
    }
}
