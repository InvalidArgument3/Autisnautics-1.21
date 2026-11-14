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
import net.minecraft.world.level.Level;

public final class ServerTickEvents {
    public static final RegisterEvent<StartWorld> START_WORLD_TICK = new RegisterEvent<>();
    public static final RegisterEvent<EndWorld> END_WORLD_TICK = new RegisterEvent<>();
    public static final RegisterEvent<StartServer> START_SERVER_TICK = new RegisterEvent<>();
    public static final RegisterEvent<EndServer> END_SERVER_TICK = new RegisterEvent<>();
    private ServerTickEvents() {}
    @FunctionalInterface public interface StartWorld { void onStartTick(Level world); }
    @FunctionalInterface public interface EndWorld { void onEndTick(Level world); }
    @FunctionalInterface public interface StartServer { void onStartTick(MinecraftServer server); }
    @FunctionalInterface public interface EndServer { void onEndTick(MinecraftServer server); }
}
