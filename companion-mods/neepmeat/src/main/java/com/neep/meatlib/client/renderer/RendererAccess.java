/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.client.renderer;

public final class RendererAccess {
    public static final RendererAccess INSTANCE = new RendererAccess();

    public Renderer getRenderer() {
        return Renderer.INSTANCE;
    }
}
