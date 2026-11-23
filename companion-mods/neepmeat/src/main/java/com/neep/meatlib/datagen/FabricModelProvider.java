/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.datagen;

import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;

public abstract class FabricModelProvider {
    public FabricModelProvider(FabricDataOutput output) {}

    public abstract void generateBlockStateModels(BlockModelGenerators generators);

    public abstract void generateItemModels(ItemModelGenerators generators);
}
