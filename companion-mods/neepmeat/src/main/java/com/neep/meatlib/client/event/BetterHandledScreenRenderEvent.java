/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. The Fabric event array is a plain listener list. REI is not registered.
 */
package com.neep.meatlib.client.event;

import com.neep.meatlib.client.screen.BetterHandledScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public interface BetterHandledScreenRenderEvent {
    void render(AbstractContainerScreen<?> screen, GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float delta);

    List<BetterHandledScreenRenderEvent> FOREGROUND = new CopyOnWriteArrayList<>();
    List<BetterHandledScreenRenderEvent> BACKGROUND = new CopyOnWriteArrayList<>();

    static void foreground(BetterHandledScreen<?> screen, GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float delta) {
        for (BetterHandledScreenRenderEvent listener : FOREGROUND) {
            listener.render(screen, graphics, x, y, mouseX, mouseY, delta);
        }
    }

    static void background(BetterHandledScreen<?> screen, GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float delta) {
        for (BetterHandledScreenRenderEvent listener : BACKGROUND) {
            listener.render(screen, graphics, x, y, mouseX, mouseY, delta);
        }
    }
}
