/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.client.networking.v1;

import com.neep.meatlib.event.RegisterEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public final class ClientPlayConnectionEvents {
    public static final RegisterEvent<Disconnect> DISCONNECT = new RegisterEvent<>();
    private ClientPlayConnectionEvents() {}
    @FunctionalInterface public interface Disconnect { void onPlayDisconnect(ClientPacketListener handler, Minecraft client); }
}
