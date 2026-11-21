/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.client.render.fluid.v1;

import net.minecraft.world.level.material.Fluid;
import java.util.HashMap;
import java.util.Map;

public final class FluidRenderHandlerRegistry {
    public static final FluidRenderHandlerRegistry INSTANCE = new FluidRenderHandlerRegistry();
    private final Map<Fluid, FluidRenderHandler> handlers = new HashMap<>();
    public void register(Fluid still, Fluid flowing, FluidRenderHandler handler) {
        handlers.put(still, handler);
        handlers.put(flowing, handler);
    }
    public FluidRenderHandler get(Fluid fluid) { return handlers.get(fluid); }
}
