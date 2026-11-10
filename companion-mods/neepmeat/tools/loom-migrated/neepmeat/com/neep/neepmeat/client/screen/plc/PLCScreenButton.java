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

package com.neep.neepmeat.client.screen.plc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.neep.meatlib.client.screen.widget.AbstractClickableWidget;
import com.neep.meatlib.client.screen.widget.TooltipRenderer;
import com.neep.meatlib.init.MeatlibSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class PLCScreenButton extends AbstractClickableWidget implements TooltipRenderer
{
    public PLCScreenButton(int x, int y, Component message)
    {
        this(x, y, 16, 16, message);
    }

    public PLCScreenButton(int x, int y, int w, int h, Component message)
    {
        super(x, y, w, h, message);
    }

    @Override
    public void renderButton(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, getTexture());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        matrices.blit(getTexture(), x0(), y0(), 0, getU(), getV() + i * getDV(), this.width, this.height, 256, 256);
    }

    protected int getYImage(boolean hovered)
    {
        return hovered ? 2 : 1;
    }

    @Override
    public void playDownSound(SoundManager soundManager)
    {
        soundManager.play(SimpleSoundInstance.forUI(MeatlibSounds.PLC_SELECT, 1.0F));
        soundManager.play(SimpleSoundInstance.forUI(MeatlibSounds.UI_BEEP, 1.0F));
    }

    protected ResourceLocation getTexture()
    {
        return PLCProgramScreen.WIDGETS;
    }

    protected int getU()
    {
        return 0;
    }

    protected int getV()
    {
        return 0;
    }

    protected int getDV()
    {
        return 16;
    }

//    @Override
//    protected void appendClickableNarrations(NarrationMessageBuilder builder)
//    {
//
//    }
}
