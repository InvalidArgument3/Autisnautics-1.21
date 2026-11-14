/*
 * Copyright (c) MeatWheeze 2026.
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

import com.neep.meatlib.client.screen.auto.BlackBackgroundPanel;
import com.neep.meatlib.client.screen.auto.Divide;
import com.neep.meatlib.client.screen.auto.HashedBackgroundPanel;
import com.neep.meatlib.client.screen.auto.SplitPanel;
import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.auto.Anchor;
import com.neep.meatlib.client.screen.slot.VanillaInventoryWidget;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepbus.client.screen.OpenNEEPBusConfigButton;
import com.neep.neepmeat.transport.client.screen.filter.FilterButtonsPanel;
import com.neep.neepmeat.transport.client.screen.filter.FilterPanel;
import com.neep.neepmeat.transport.client.screen.widget.RequesterConfigWidget;
import com.neep.neepmeat.transport.screen.FilterScreenHandler;
import com.neep.neepmeat.transport.screen.RequesterScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class RequesterScreen extends BetterHandledScreen<RequesterScreenHandler>
{
    public RequesterScreen(RequesterScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
    }

    @Override
    protected void init()
    {
        imageWidth = FilterScreenHandler.BACKGROUND_WIDTH + 90;
        imageHeight = FilterScreenHandler.BACKGROUND_HEIGHT;

        super.init();

        var playerInv = addSlotBlock(new VanillaInventoryWidget(menu.playerSlots, this));

        var filters = new HashedBackgroundPanel(new BlackBackgroundPanel(new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                .setLeft(new FilterPanel(menu.filterHandler, menu::getCarried))
                .setRight(new FilterButtonsPanel(menu.filterHandler, 1, 1)
                        .addChild(new OpenNEEPBusConfigButton(() -> menu.openNeepBusC2S.emitter().run())))
                .setFit(SplitPanel.Fit.SECOND)
        ));

        var main = new SplitPanel(
                        SplitPanel.Split.VERTICAL, 0)
                        .setLeft(
                            new SplitPanel(
                                SplitPanel.Split.HORIZONTAL, 0)
                                .setLeft(filters)
                                .setRight(new HashedBackgroundPanel(new BlackBackgroundPanel(
                                        new RequesterConfigWidget(menu.requesterConfigHandler))))
                                .setDivide(new Divide.Abs(Anchor.START, FilterScreenHandler.BACKGROUND_WIDTH))
                        )
                        .setRight(playerInv)
                        .setFit(SplitPanel.Fit.SECOND)
                        .centre(true)
                ;

        addRenderableWidget(main);
        main.setDims(leftPos, topPos, imageWidth, imageHeight);

        ScreenReady.ready();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }
}
