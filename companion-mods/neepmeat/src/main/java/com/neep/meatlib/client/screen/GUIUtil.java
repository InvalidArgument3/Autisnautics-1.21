/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * The part of the meatlib screen kit the kiln and tank screens need: drawing a
 * gui texture. The rest of the panel kit is still unported.
 */

package com.neep.meatlib.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class GUIUtil {
    private GUIUtil() {}

    public static void drawTexture(ResourceLocation texture, GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(texture, x, y, u, v, width, height);
    }
}
