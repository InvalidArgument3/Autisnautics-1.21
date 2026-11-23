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
import net.minecraft.client.Minecraft;

public final class ClientTickEvents {
    public static final RegisterEvent<StartTick> START_CLIENT_TICK = new RegisterEvent<>();
    public static final RegisterEvent<EndTick> END_CLIENT_TICK = new RegisterEvent<>();
    private ClientTickEvents() {}

    @FunctionalInterface public interface StartTick { void onStartTick(Minecraft client); }
    @FunctionalInterface public interface EndTick { void onEndTick(Minecraft client); }
}
