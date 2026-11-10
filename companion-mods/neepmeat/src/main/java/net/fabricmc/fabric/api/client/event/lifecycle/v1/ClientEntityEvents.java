/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.client.event.lifecycle.v1;

import com.neep.meatlib.event.RegisterEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public final class ClientEntityEvents {
    public static final RegisterEvent<Load> ENTITY_LOAD = new RegisterEvent<>();
    private ClientEntityEvents() {}
    @FunctionalInterface public interface Load { void onLoad(Entity entity, ClientLevel world); }
}
