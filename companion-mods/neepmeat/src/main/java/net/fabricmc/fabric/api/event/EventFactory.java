/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */


package net.fabricmc.fabric.api.event;

import java.lang.reflect.Proxy;

public final class EventFactory {
    private EventFactory() {}

    @SuppressWarnings("unchecked")
    public static <T> Event<T> createArrayBacked(Class<T> type, Object... ignored) {
        T invoker = (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            Class<?> ret = method.getReturnType();
            if (ret == boolean.class) return false;
            if (ret == int.class || ret == long.class || ret == short.class || ret == byte.class) return 0;
            if (ret == float.class || ret == double.class) return 0f;
            if (ret == void.class) return null;
            if (ret.getName().endsWith("InteractionResult")) return Enum.valueOf((Class<Enum>) ret, "PASS");
            return null;
        });
        return new Event<>(invoker);
    }

    public static <T> Event<T> createWithPhases(Class<T> type, Object... ignored) {
        return createArrayBacked(type);
    }
}
