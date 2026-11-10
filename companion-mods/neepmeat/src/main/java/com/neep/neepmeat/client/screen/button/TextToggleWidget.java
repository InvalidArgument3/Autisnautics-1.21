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
import com.neep.meatlib.theme.PLCCols;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import com.neep.meatlib.client.screen.GUIUtil;

import static com.neep.meatlib.client.screen.button.NMButtonWidget.NM_WIDGETS_TEXTURE;

public class TextToggleWidget extends AbstractWidget implements GUIUtil
{
    protected boolean toggled;
    protected ToggleAction onToggle;

    public TextToggleWidget(int x, int y, int width, int height, Component message, boolean toggled, ToggleAction onToggle)
    {
        super(x, y, width, height, message);
        this.toggled = toggled;
        this.onToggle = onToggle;
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        this.toggled = !this.toggled;

        this.onToggle.onToggle(this, toggled);
    }

    public void renderTooltip(GuiGraphics matrices, int mouseX, int mouseY)
    {
//        this.tooltipSupplier.onTooltip(this, matrices, mouseX, mouseY);
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        this.renderMain(context, mouseX, mouseY, delta);
        if (this.isHovered())
        {
            this.renderTooltip(context, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder)
    {

    }

    protected void renderMain(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        Minecraft minecraftClient = Minecraft.getInstance();
        Font textRenderer = minecraftClient.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        int i = this.getYImage(toggled);
//        int i = 0; // TODO
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int borderCol = isHovered() || toggled ? PLCCols.SELECTED.col : PLCCols.BORDER.col;
        int textCol = isHovered() ? PLCCols.SELECTED.col : PLCCols.BORDER.col;

        matrices.blit(NM_WIDGETS_TEXTURE, getX(), getY(), 0, 90, this.width / 2, this.height);
        matrices.blit(NM_WIDGETS_TEXTURE, getX() + this.width / 2, getY(), 200 - this.width / 2, 90, this.width / 2, this.height);

        GUIUtil.renderBorder(matrices, getX() + 3, getY() + 3, width - 4 * 2 + 1, height - 4 * 2 + 1, borderCol, 0);

        GUIUtil.drawCenteredText(matrices, textRenderer, this.getMessage(), getX() + this.width / 2f, getY() + (this.height - 8) / 2f, textCol, false);
    }

    int getYImage(boolean toggled)
    {
        return toggled ? 2 : 1;
    }

    public interface ToggleAction
    {
        void onToggle(TextToggleWidget button, boolean toggled);
    }
}
