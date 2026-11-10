/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */


package net.fabricmc.fabric.api.event;

import java.util.ArrayList;
import java.util.List;

public final class Event<T> {
    private final List<T> listeners = new ArrayList<>();
    private final T invoker;

    public Event(T invoker) { this.invoker = invoker; }

    public void register(T listener) { if (listener != null) listeners.add(listener); }
    public T invoker() { return invoker; }
    public List<T> listeners() { return List.copyOf(listeners); }
}
