/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.live_machine;

public record PropertyValue(Function function, float value) {
    public PropertyValue(float value) {
        this(Function.AVERAGE, value);
    }

    public float apply(float current, int count) {
        return switch (function) {
            case ADD -> current + value;
            case AVERAGE -> current + (value / count);
        };
    }

    public enum Function {
        ADD,
        AVERAGE;

        public boolean average() {
            return this == AVERAGE;
        }
    }
}
