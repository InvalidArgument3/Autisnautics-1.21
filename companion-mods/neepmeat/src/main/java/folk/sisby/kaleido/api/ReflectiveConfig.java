/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */


package folk.sisby.kaleido.api;

import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

/** Kaleido config base. Fields stay {@code value(default)}. */
public class ReflectiveConfig {
    protected <T> TrackedValue<T> value(T value) {
        return new TrackedValue<>(value);
    }
}
