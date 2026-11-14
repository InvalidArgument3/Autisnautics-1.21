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

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.screen_handler.AlloyKilnScreenHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@OnlyIn(Dist.CLIENT)
public class AlloyKilnScreen extends AbstractContainerScreen<AlloyKilnScreenHandler>
{
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/alloy_kiln.png");

    private final AlloyKilnScreenHandler handler;

    public AlloyKilnScreen(AlloyKilnScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        this.handler = handler;
        imageWidth = 176;
        imageHeight = 176;
    }

    @Override
    protected void renderBg(GuiGraphics matrices, float delta, int mouseX, int mouseY)
    {
//        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int i = this.leftPos;
        int j = this.topPos;
        matrices.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        drawBurnTime(matrices, i, j);
        drawProgress(matrices, i, j);
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        renderBackground(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
        renderTooltip(matrices, mouseX, mouseY);
    }

    public void drawBurnTime(GuiGraphics matrices, int i, int j)
    {
        int time = handler.getProperty(0);
        if (time < 1)
            return;

        int total = handler.getProperty(1);
        int k = (int) ((time / (total + 1f)) * 14);
        matrices.blit(TEXTURE, i + 57, j + 37 + 12 - k, 176, 12 - k, 14, k + 1);
    }

    public void drawProgress(GuiGraphics matrices, int i, int j)
    {
        int time = handler.getProperty(2);
        if (time < 1)
            return;

        int total = handler.getProperty(3);
        int k = (int) ((time / (total + 1f)) * 24);
        matrices.blit(TEXTURE, i + 80, j + 36, 176, 14, k, 17);
    }

    @Override
    protected void init()
    {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }
}
