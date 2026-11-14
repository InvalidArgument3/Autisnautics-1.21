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
import com.neep.meatlib.client.screen.auto.BlackBackgroundPanel;
import com.neep.meatlib.client.screen.auto.HashedBackgroundPanel;
import com.neep.meatlib.client.screen.auto.Panel;
import com.neep.meatlib.client.screen.auto.SplitPanel;
import com.neep.meatlib.client.screen.slot.VanillaInventoryWidget;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepmeat.transport.client.screen.filter.AdvancedEjectorConfigWidget;
import com.neep.neepmeat.transport.client.screen.filter.FilterButtonsPanel;
import com.neep.neepmeat.transport.client.screen.filter.FilterPanel;
import com.neep.neepmeat.transport.screen.AdvancedEjectorScreenHandler;
import com.neep.neepmeat.transport.screen.FilterScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedEjectorScreen extends BetterHandledScreen<AdvancedEjectorScreenHandler>
{
    private final VanillaInventoryWidget playerInventory;
    private final Panel mainPanel;

    public AdvancedEjectorScreen(AdvancedEjectorScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);

        playerInventory = new VanillaInventoryWidget(0, 0, handler.slots, this);

        var rightPanel = new HashedBackgroundPanel(
                new BlackBackgroundPanel(
                        new AdvancedEjectorConfigWidget(handler.ejectorHandler, 1, 1, false)
                        ,
                        3),
                3);

        rightPanel.setSize(90, 0);

        mainPanel = new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                .setLeft(new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                        .setLeft(
                            new HashedBackgroundPanel(new BlackBackgroundPanel(
                                    new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                                        .setLeft(new FilterPanel(handler.filterHandler, handler::getCarried))
                                        .setRight(new FilterButtonsPanel(handler.filterHandler, 1, 1))
                                        .setFit(SplitPanel.Fit.SECOND),
                            3), 3))
                        .setRight(rightPanel)
                        .setFit(SplitPanel.Fit.SECOND)
                )
                .setRight(playerInventory)
                .setFit(SplitPanel.Fit.SECOND)
                .centre(true);

        ScreenReady.ready();
    }

    @Override
    protected void init()
    {
        imageWidth = FilterScreenHandler.BACKGROUND_WIDTH + 90;
        imageHeight = FilterScreenHandler.BACKGROUND_HEIGHT;

        super.init();

        mainPanel.setDims(leftPos, topPos, imageWidth, imageHeight);

        addSlotBlock(playerInventory);
        addRenderableWidget(mainPanel);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY)
    {
        renderBackground(context, mouseX, mouseY, delta);
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY)
    {
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }
}
