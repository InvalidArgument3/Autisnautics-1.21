/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.client.particle.v1;

import com.neep.meatlib.event.FabricClientEvents;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public final class ParticleFactoryRegistry {
    private static final ParticleFactoryRegistry INSTANCE = new ParticleFactoryRegistry();

    public static ParticleFactoryRegistry getInstance() { return INSTANCE; }

    public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleProvider<T> provider) {
        FabricClientEvents.particle(type, provider);
    }

    public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleEngine.SpriteParticleRegistration<T> registration) {
        FabricClientEvents.particleSprite(type, registration);
    }
}
