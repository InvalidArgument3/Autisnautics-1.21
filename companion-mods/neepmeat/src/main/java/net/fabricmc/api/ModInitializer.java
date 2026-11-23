/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * The class is the entrypoint. NeoForge calls the constructor, then onInitialize.
 */

package net.fabricmc.api;

public interface ModInitializer {
    void onInitialize();
}
