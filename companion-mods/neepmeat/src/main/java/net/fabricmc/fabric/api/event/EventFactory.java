/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package net.fabricmc.fabric.api.event;

import java.util.function.Function;

public final class EventFactory {
    private EventFactory() {}

    public static <T> Event<T> createArrayBacked(Class<T> type, Function<T[], T> invokerFactory) {
        return new Event<>(type, invokerFactory);
    }

    public static <T> Event<T> createArrayBacked(Class<T> type, T empty, Function<T[], T> invokerFactory) {
        return new Event<>(type, listeners -> listeners.length == 0 ? empty : invokerFactory.apply(listeners));
    }

    public static <T> Event<T> createWithPhases(Class<T> type, Function<T[], T> invokerFactory, Object... phases) {
        return createArrayBacked(type, invokerFactory);
    }
}
