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

package com.neep.meatlib.client.screen;

import com.neep.meatlib.client.event.BetterHandledScreenRenderEvent;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class REIScreenSupport
{
    public REIScreenSupport()
    {
        BetterHandledScreenRenderEvent.FOREGROUND.register(this::renderForeground);
        BetterHandledScreenRenderEvent.BACKGROUND.register(this::renderBackground);
    }

    // These are Architectury events, but this class is only active when REI is loaded.
    public void renderForeground(AbstractContainerScreen<?> handledScreen, GuiGraphics matrices, int x, int y, int mouseX, int mouse, float delta)
    {
        ClientGuiEvent.RENDER_CONTAINER_FOREGROUND.invoker().render(handledScreen, matrices, mouseX, mouse, delta);
    }

    public void renderBackground(AbstractContainerScreen<?> handledScreen, GuiGraphics matrices, int x, int y, int mouseX, int mouse, float delta)
    {
        ClientGuiEvent.RENDER_CONTAINER_BACKGROUND.invoker().render(handledScreen, matrices, mouseX, mouse, delta);
    }
}
