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

package com.neep.meatlib.client.screen.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.neep.meatlib.MeatLib;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.theme.PLCCols;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.primitive.Rectangle;

/**
 * A silly, janky button widget that blends in with the gas plasma display theme.
 * Does not implement {@link Rectangle.Mutable} as some buttons may not want to be resized.
 */
public class NMButtonWidget extends Button implements ClickableWidget, Rectangle.Mutable
{
    public static final ResourceLocation NM_WIDGETS_TEXTURE = GUIUtil.INVENTORY_BACKGROUND;
    protected boolean showBackground = true;

    public NMButtonWidget(int x, int y, int width, int height, Component message, OnPress onPress)
    {
        super(x, y, width, height, message, onPress, textSupplier -> Component.empty());
    }

    public NMButtonWidget showBackground(boolean background)
    {
        this.showBackground = background;
        return this;
    }

    protected boolean borderActive()
    {
        return isHovered();
    }

    protected boolean textActive()
    {
        return borderActive();
    }

    @Override
    public void renderWidget(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        Minecraft minecraftClient = Minecraft.getInstance();
        if (showBackground)
        {
            matrices.setColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            GUIUtil.drawNineSlicedTexture(matrices, NM_WIDGETS_TEXTURE, getX(), getY(), this.getWidth(), this.getHeight(), 4, 200, 20, 0, this.getTextureY());
//            matrices.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        int borderCol = borderCol();

        if (showBackground)
            GUIUtil.renderBorderInner(matrices, this.x0() + 3, this.y0() + 3, width - 4 * 2 + 2, height - 4 * 2 + 2, borderCol, 0);
        else
            GUIUtil.renderBorderInner(matrices, this.x0(), this.y0(), width, height, borderCol, 0);

        int textCol = textActive() ? PLCCols.SELECTED.col : PLCCols.TEXT.col;
        this.renderString(matrices, minecraftClient.font, textCol);
    }

    @Override
    public void playDownSound(SoundManager soundManager)
    {
        soundManager.play(SimpleSoundInstance.forUI(MeatlibSounds.PLC_SELECT, 1.0F));
        soundManager.play(SimpleSoundInstance.forUI(MeatlibSounds.UI_BEEP, 1.0F));
    }

    protected int borderCol()
    {
        return borderActive() ? PLCCols.SELECTED.col : PLCCols.BORDER.col;
    }

    private int getTextureY()
    {
        return 90; // V offset for button background
//        int i = 1;
//        if (!this.active)
//        {
//            i = 0;
//        }
//        else if (this.isSelected())
//        {
//            i = 2;
//        }
//
//        return 46 + i * 20;
    }

    @Override
    public void renderString(GuiGraphics context, Font textRenderer, int color)
    {
        this.renderScrollingString(context, textRenderer, 4, color);
    }

    @Override
    public int x0()
    {
        return getX();
    }

    @Override
    public int y0()
    {
        return getY();
    }

    @Override
    public int nw()
    {
        return width;
    }

    @Override
    public int nh()
    {
        return height;
    }

    @Override
    public void setPos(int x, int y)
    {
        setX(x);
        setY(y);
    }

    @Override
    public void setDims(int x, int y, int w, int h)
    {
        setPos(x, y);
        width = w;
        height = h;
    }
}
