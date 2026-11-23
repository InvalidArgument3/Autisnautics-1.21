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

public final class ServerLifecycleEvents {
    public static final RegisterEvent<Server> SERVER_STARTING = new RegisterEvent<>();
    public static final RegisterEvent<Server> SERVER_STARTED = new RegisterEvent<>();
    public static final RegisterEvent<Server> SERVER_STOPPING = new RegisterEvent<>();
    public static final RegisterEvent<Server> SERVER_STOPPED = new RegisterEvent<>();
    private ServerLifecycleEvents() {}
    @FunctionalInterface public interface Server { void onServer(MinecraftServer server); }
}
