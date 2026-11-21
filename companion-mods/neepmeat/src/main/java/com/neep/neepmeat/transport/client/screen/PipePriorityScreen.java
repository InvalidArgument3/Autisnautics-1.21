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

package com.neep.neepmeat.transport.client.screen;

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepmeat.transport.screen.PipePriorityScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PipePriorityScreen extends BetterHandledScreen<PipePriorityScreenHandler>
{
    private final PipePriorityWidget priorityWidget;

    public PipePriorityScreen(PipePriorityScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        priorityWidget = new PipePriorityWidget(0, 0, 200, 100, handler.priorityHandler);

        ScreenReady.ready();
    }

    @Override
    protected void init()
    {
        super.init();

        addRenderableWidget(priorityWidget);
        priorityWidget.setW(imageWidth);
        imageHeight = priorityWidget.nh();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        priorityWidget.setPos(leftPos, topPos);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY)
    {
        renderBackground(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClose()
    {
        priorityWidget.update();
        super.onClose();
    }
}
