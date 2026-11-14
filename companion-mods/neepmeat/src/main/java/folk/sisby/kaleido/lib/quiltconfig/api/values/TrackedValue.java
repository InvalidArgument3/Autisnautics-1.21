/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package folk.sisby.kaleido.lib.quiltconfig.api.values;

import java.util.function.Supplier;

/** Kaleido field holder. {@link com.neep.meatlib.config.ConfigFiles} binds it to a NeoForge spec. */
public final class TrackedValue<T> {
    private final T fallback;
    private Supplier<T> live;

    public TrackedValue(T value) {
        this.fallback = value;
        this.live = () -> fallback;
    }

    public void bind(Supplier<T> live) {
        this.live = live == null ? () -> fallback : live;
    }

    public T getRealValue() {
        T value = live.get();
        return value == null ? fallback : value;
    }

    public T value() {
        return getRealValue();
    }

    public T getDefaultValue() {
        return fallback;
    }
}
