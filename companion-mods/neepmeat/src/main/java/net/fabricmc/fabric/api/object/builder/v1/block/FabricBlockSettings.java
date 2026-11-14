/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Delegates to MeatlibBlockSettings, which unwraps to BlockBehaviour.Properties.
 */

package net.fabricmc.fabric.api.object.builder.v1.block;

import com.neep.meatlib.block.MeatlibBlockSettings;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class FabricBlockSettings {
    private final MeatlibBlockSettings inner;

    private FabricBlockSettings(MeatlibBlockSettings inner) {
        this.inner = inner;
    }

    public static FabricBlockSettings of() {
        return new FabricBlockSettings(MeatlibBlockSettings.of());
    }

    public static FabricBlockSettings copyOf(net.minecraft.world.level.block.Block block) {
        return new FabricBlockSettings(MeatlibBlockSettings.copyOf(block));
    }

    public FabricBlockSettings strength(float strength) {
        inner.strength(strength);
        return this;
    }

    public FabricBlockSettings sounds(SoundType sound) {
        inner.sounds(sound);
        return this;
    }

    public FabricBlockSettings nonOpaque() {
        inner.nonOpaque();
        return this;
    }

    public BlockBehaviour.Properties properties() {
        return inner.properties();
    }
}
