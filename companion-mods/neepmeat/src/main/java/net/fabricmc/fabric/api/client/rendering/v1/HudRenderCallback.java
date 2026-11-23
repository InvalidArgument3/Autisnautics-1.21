/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.client.rendering.v1;

import com.neep.meatlib.event.RegisterEvent;
import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface HudRenderCallback {
    RegisterEvent<HudRenderCallback> EVENT = new RegisterEvent<>();

    void onHudRender(GuiGraphics graphics, float tickDelta);
}
