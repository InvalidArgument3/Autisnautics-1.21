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

package com.neep.neepmeat.client.screen;

import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.theme.PLCCols;
import com.neep.meatlib.client.screen.button.NMButtonWidget;
import com.neep.meatlib.client.screen.GUIUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@OnlyIn(Dist.CLIENT)
public class CyclingButtonWidget extends NMButtonWidget implements Rectangle.Mutable
{
    private final int du;
    private final int dv;
    protected final ResourceLocation texture;
    protected final int u;
    protected final int v;
    protected final int tw;
    protected final int th;
    protected final int vOffset;
    protected final int maxIndex;
    protected int index;

    @OnlyIn(Dist.CLIENT)
    public CyclingButtonWidget(int x, int y, int width, int height, int u, int v, int du, int dv, int vOffset, int maxIndex, ResourceLocation texture, int tw, int th, Component message, OnPress onPress)
    {
        super(x, y, width, height, message, onPress);
        this.du = du;
        this.dv = dv;

        this.texture = texture;
        this.u = u;
        this.v = v;
        this.tw = tw;
        this.th = th;
        this.vOffset = vOffset;
        this.maxIndex = maxIndex;
        this.index = 0;
    }

    @Override
    public void renderWidget(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
//        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
//        RenderSystem.setShaderTexture(0, this.texture);
//        RenderSystem.enableDepthTest();
        int w = nw();
        GUIUtil.renderBorderInner(matrices, this.x0(), this.y0(), w, nh(), PLCCols.BORDER.col, 0);
        int texStart = this.x0() + (w - du) / 2;
        renderTexture(matrices, texture, texStart, this.y0(), u, v + vOffset * index, 0, du, dv, tw, th);
        if (this.isHovered())
        {
//            super.renderTooltip(matrices, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (!this.active || !this.visible)
        {
            return false;
        }
        if (this.isValidClickButton(button) && (this.clicked(mouseX, mouseY)))
        {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
            return true;
        }
        return false;
    }

    @Override
    protected boolean isValidClickButton(int button)
    {
        return button == 0 || button == 1;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }
}
