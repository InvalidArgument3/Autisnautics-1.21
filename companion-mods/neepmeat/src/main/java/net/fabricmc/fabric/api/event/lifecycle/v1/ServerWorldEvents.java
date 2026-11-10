/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.event.lifecycle.v1;

import com.neep.meatlib.event.RegisterEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class ServerWorldEvents {
    public static final RegisterEvent<Load> LOAD = new RegisterEvent<>();
    public static final RegisterEvent<Unload> UNLOAD = new RegisterEvent<>();
    private ServerWorldEvents() {}
    @FunctionalInterface public interface Load { void onWorldLoad(MinecraftServer server, ServerLevel world); }
    @FunctionalInterface public interface Unload { void onWorldUnload(MinecraftServer server, ServerLevel world); }
}
