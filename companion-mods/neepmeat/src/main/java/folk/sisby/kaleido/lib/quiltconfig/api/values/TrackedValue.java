/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */


package folk.sisby.kaleido.lib.quiltconfig.api.values;

public final class TrackedValue<T> {
    private final T value;

    public TrackedValue(T value) { this.value = value; }

    public T getRealValue() { return value; }
    public T value() { return value; }
    public T getDefaultValue() { return value; }
}
