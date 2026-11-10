/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.neepasm;

/**
 * The compiler and VM land in a later batch. The arm already throws this when it
 * refuses a move.
 */
public final class NeepASM {
    private NeepASM() {}

    public static class RuntimeException extends Exception {
        public RuntimeException(String message) {
            super(message);
        }
    }
}
