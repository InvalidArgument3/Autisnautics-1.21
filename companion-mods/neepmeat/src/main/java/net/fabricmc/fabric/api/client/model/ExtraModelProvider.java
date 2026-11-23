/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.client.model;

import net.minecraft.resources.ResourceLocation;
import java.util.function.Consumer;

public interface ExtraModelProvider {
    void provideExtraModels(Consumer<ResourceLocation> out);
}
