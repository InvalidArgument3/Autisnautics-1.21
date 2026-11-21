/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.datagen;

import net.minecraft.advancements.Advancement;

import java.util.function.Consumer;

public abstract class FabricAdvancementProvider {
    public FabricAdvancementProvider(FabricDataOutput output) {}

    public abstract void generateAdvancement(Consumer<Advancement> consumer);
}
