/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.meatlib.client.event;

import com.neep.meatlib.client.screen.BetterHandledScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Render events invoked by {@link BetterHandledScreen}.
 * Used for manually getting EMI and REI to render.
 */
@Environment(EnvType.CLIENT)
public interface BetterHandledScreenRenderEvent
{
    Event<BetterHandledScreenRenderEvent> FOREGROUND = EventFactory.createArrayBacked(BetterHandledScreenRenderEvent.class, listeners -> ((handledScreen, matrices, x, y, mouseX, mouseY, delta) ->
    {
        for (var listener : listeners)
        {
            listener.render(handledScreen, matrices, x, y, mouseX, mouseY, delta);
        }
    }));

    Event<BetterHandledScreenRenderEvent> BACKGROUND = EventFactory.createArrayBacked(BetterHandledScreenRenderEvent.class, listeners -> ((handledScreen, matrices, x, y, mouseX, mouseY, delta) ->
    {
        for (var listener : listeners)
        {
            listener.render(handledScreen, matrices, x, y, mouseX, mouseY, delta);
        }
    }));

    void render(AbstractContainerScreen<?> handledScreen, GuiGraphics matrices, int x, int y, int mouseX, int mouseY, float delta);
}
