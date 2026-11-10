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

import com.neep.meatlib.client.screen.widget.IntNumberTextField;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepmeat.screen_handler.DisplayPlateScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DisplayPlateScreen extends BaseHandledScreen<DisplayPlateScreenHandler>
{
    private final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/vsc.png");

    public DisplayPlateScreen(DisplayPlateScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
    }

    @Override
    protected void init()
    {
        this.imageWidth = 128;
        this.imageHeight = 32;

        super.init();

        com.neep.meatlib.client.screen.NMTextField textField = new IntNumberTextField(this.font, leftPos + 6, topPos + 7, imageWidth - 10, 17,
                NeepMeat.translationKey("screen", "display_plate.capacity"), (c) -> {}, 0, 8, 1, 1);
        addRenderableWidget(textField);
        textField.drawFancyBackground(false);
        textField.setText(String.valueOf(menu.getCapacity()));

        textField.setTextPredicate(s ->
        {
            try
            {
                int parsed = !s.isEmpty() && s.matches("-?[0-9]*") ? Integer.parseInt(s) : 0;
                return parsed >= 0 && parsed <= 8;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        });

        textField.setChangedListener(s ->
        {
            try
            {
                int parsed = !s.isEmpty() && s.matches("[0-9]*") ? Integer.parseInt(s) : 0;
                menu.setCapacity(parsed);
            }
            catch (NumberFormatException e)
            {
                menu.setCapacity(1);
            }
        });
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY)
    {
        renderBackground(context);
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
//        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        GUIUtil.drawScreenBorder(context, i, j, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY)
    {
    }
}
