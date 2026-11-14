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

package com.neep.neepmeat.machine.small_compressor;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import com.neep.meatlib.client.screen.GUIUtil;

public class SmallCompressorScreen extends AbstractContainerScreen<SmallCompressorScreenHandler>
{
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/small_compressor.png");

    public SmallCompressorScreen(SmallCompressorScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
    }

    @Override
    protected void init()
    {
        this.imageWidth = 176;
        this.imageHeight = 90 + 1 + 90;
        super.init();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        renderBackground(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);

        renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics matrices, float delta, int mouseX, int mouseY)
    {
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
//        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        GUIUtil.drawTexture(TEXTURE, matrices, i, j, 0, 0, this.imageWidth, this.imageHeight);

        int burnTime = menu.getProperty(0);
        int maxBurnTime = menu.getProperty(1);
        if (burnTime >= 0 && maxBurnTime > 0)
        {
            float f = (float) burnTime / maxBurnTime;
            int height = (int) (f * 14);
            GUIUtil.drawTexture(TEXTURE, matrices,  i + 56 + 24, j + 36 + 14 - height, 176, 14 - height, 14, height);
        }

        GUIUtil.drawInventoryBackground(matrices, i, j + 91);
    }

    @Override
    protected void renderLabels(GuiGraphics matrices, int mouseX, int mouseY)
    {
    }
}
