/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.datagen;

import net.minecraft.data.PackOutput;

import java.nio.file.Path;

/** Fabric's data output. It is a {@link PackOutput}. */
public class FabricDataOutput extends PackOutput {
    public FabricDataOutput(Path path) {
        super(path);
    }
}
