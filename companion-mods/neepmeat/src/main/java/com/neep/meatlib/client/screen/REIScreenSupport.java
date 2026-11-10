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
 *
 * Architectury's container render events are NeoForge {@code ContainerScreenEvent}.
 */

package com.neep.meatlib.client.screen;

import com.neep.meatlib.client.event.BetterHandledScreenRenderEvent;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public class REIScreenSupport
{
    public REIScreenSupport()
    {
        NeoForge.EVENT_BUS.addListener(this::onForeground);
        NeoForge.EVENT_BUS.addListener(this::onBackground);
    }

    private void onForeground(ContainerScreenEvent.Render.Foreground event)
    {
        renderForeground(event.getContainerScreen(), event.getGuiGraphics(), 0, 0, event.getMouseX(), event.getMouseY(), 0);
    }

    private void onBackground(ContainerScreenEvent.Render.Background event)
    {
        renderBackground(event.getContainerScreen(), event.getGuiGraphics(), 0, 0, event.getMouseX(), event.getMouseY(), 0);
    }

    public void renderForeground(AbstractContainerScreen<?> handledScreen, GuiGraphics matrices, int x, int y, int mouseX, int mouse, float delta)
    {
        for (BetterHandledScreenRenderEvent listener : List.copyOf(BetterHandledScreenRenderEvent.FOREGROUND)) {
            listener.render(handledScreen, matrices, x, y, mouseX, mouse, delta);
        }
    }

    public void renderBackground(AbstractContainerScreen<?> handledScreen, GuiGraphics matrices, int x, int y, int mouseX, int mouse, float delta)
    {
        for (BetterHandledScreenRenderEvent listener : List.copyOf(BetterHandledScreenRenderEvent.BACKGROUND)) {
            listener.render(handledScreen, matrices, x, y, mouseX, mouse, delta);
        }
    }
}
