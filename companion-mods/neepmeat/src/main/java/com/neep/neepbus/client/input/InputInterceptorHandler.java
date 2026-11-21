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

package com.neep.neepbus.client.input;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public interface InputInterceptorHandler
{
    void render(GuiGraphics matrices, float tickDelta);

    default boolean onRenderOutline(WorldRenderContext wrctx, WorldRenderContext.BlockOutlineContext ctx) { return true; }

    default void overrideCamera(Minecraft client, Camera camera, float tickDelta) { }

    default boolean acceptScreen(Screen screen) { return false; }

    default boolean requiresScreen() { return false; }

    default boolean hideHud() { return false; }

    @FunctionalInterface
    interface Factory<T extends InputInterceptorHandler>
    {
        T create();
    }
}
