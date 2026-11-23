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

package com.neep.neepbus.client.screen;

import com.neep.neepbus.screen.PIDControllerScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PIDControllerScreen extends NeepBusConfigScreen<PIDControllerScreenHandler>
{
    private final PIDConfigWidget pidConfig;

    public PIDControllerScreen(PIDControllerScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        this.pidConfig = new PIDConfigWidget(0, 0, 100, 100, handler.pidHandler);
    }

    @Override
    protected void init()
    {
        super.init();

        int wHeight = minecraft.getWindow().getGuiScaledHeight();

        addRenderableWidget(pidConfig);

        // Attach the range config to the bottom of the port config
        pidConfig.setW(imageWidth);
        pidConfig.init();

        pidConfig.setPos(leftPos, topPos + imageHeight + 12);
        imageHeight += pidConfig.nh();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY)
    {
        renderBackground(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClose()
    {
        pidConfig.update();
        super.onClose();
    }
}
