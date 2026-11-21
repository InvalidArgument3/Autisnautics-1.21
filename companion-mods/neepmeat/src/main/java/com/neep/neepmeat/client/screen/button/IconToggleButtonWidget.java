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

package com.neep.neepmeat.client.screen.button;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

// TODO
public class IconToggleButtonWidget extends TextToggleButtonWidget
{
    private final ResourceLocation texture;

    public IconToggleButtonWidget(int x, int y, int width, int height, ResourceLocation texture, int u, int v, BooleanSupplier toggled, Component message, ToggleAction onToggle)
    {
        super(x, y, width, height, toggled, message, onToggle);
        this.texture = texture;
    }

    @Override
    public void renderWidget(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        matrices.disableScissor();
        if (showBackground)
        {
            matrices.setColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
//            GUIUtil.drawNineSlicedTexture(matrices, NM_WIDGETS_TEXTURE, getX(), getY(), this.getWidth(), this.getHeight(), 4, 200, 20, 0, this.getTextureY());
        }

//        int borderCol = borderActive() ? PLCCols.SELECTED.col : PLCCols.BORDER.col;
//        GUIUtil.renderBorder(matrices, getX() + 3, getY() + 3, width - 4 * 2 + 1, height - 4 * 2 + 1, borderCol, 0);
//        GUIUtil.drawTexture(texture, getX()
//        int textCol = textActive() ? PLCCols.SELECTED.col : PLCCols.TEXT.col;
//        this.drawMessage(matrices, minecraftClient.textRenderer, textCol);
    }
}
