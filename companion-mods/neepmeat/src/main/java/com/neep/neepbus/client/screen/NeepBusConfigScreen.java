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

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepbus.screen.NeepBusConfigScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class NeepBusConfigScreen<T extends NeepBusConfigScreenHandler> extends BetterHandledScreen<T>
{
    private final AddressConfigWidget configWidget;

    public NeepBusConfigScreen(T handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        configWidget = new AddressConfigWidget(handler.configHandler);

    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY)
    {
        renderBackground(context, mouseX, mouseY, delta);
    }

    @Override
    protected void init()
    {
        ScreenReady.ready();
        
        addRenderableWidget(configWidget);
        configWidget.init();

        imageWidth = configWidget.nw();
        imageHeight = configWidget.nh();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        configWidget.setPos(leftPos, topPos);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY)
    {
    }
}
