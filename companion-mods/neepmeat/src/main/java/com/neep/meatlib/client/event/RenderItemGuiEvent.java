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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.render.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;

// CURRENTLY BROKEN
public interface RenderItemGuiEvent
{
    Event<RenderItemGuiEvent> EVENT = EventFactory.createArrayBacked(RenderItemGuiEvent.class,
            (listeners) -> (context, textRenderer, stack, x, y) ->
            {
                for (RenderItemGuiEvent listener : listeners)
                {
                    listener.render(context, textRenderer, stack, x, y);
                }
            });

    void render(GuiGraphics context, Font textRenderer, ItemStack stack, int x, int y);

    static void renderGuiQuad(BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha)
    {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(x + 0, y + 0, 0.0).setColor(red, green, blue, alpha);
        buffer.vertex(x + 0, y + height, 0.0).setColor(red, green, blue, alpha);
        buffer.vertex(x + width, y + height, 0.0).setColor(red, green, blue, alpha);
        buffer.vertex(x + width, y + 0, 0.0).setColor(red, green, blue, alpha);
        var built = buffer.end();
        BufferUploader.drawWithShader(built);
    }
}
