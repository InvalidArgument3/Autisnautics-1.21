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
import com.neep.meatlib.client.screen.auto.Panel;
import com.neep.meatlib.client.screen.auto.VStackPanel;
import com.neep.meatlib.client.screen.widget.config.IntListConfigWidget;
import com.neep.meatlib.screen.NumberListConfigHandler;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepbus.screen.DelayScreenHandler;
import com.neep.neepmeat.NeepMeat;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DelayScreen extends BetterHandledScreen<DelayScreenHandler>
{
    private final Panel mainPanel;
    private final AddressConfigWidget configWidget;
    private final DelayConfigWidget delayConfig;

    public DelayScreen(DelayScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        this.configWidget = new AddressConfigWidget(handler.configHandler);
        this.delayConfig = new DelayConfigWidget(0, 0, 100, 100, handler.delayHandler);

        this.mainPanel = new VStackPanel(1, 12, true)
                .addChild(configWidget)
                .addChild(delayConfig);

        ScreenReady.ready();
    }

    @Override
    protected void init()
    {
        configWidget.init();
        imageWidth = configWidget.nw();
        imageHeight = configWidget.nh() + 20;

        super.init();

        delayConfig.setW(configWidget.nw());

        mainPanel.setDims(leftPos, topPos, imageWidth, imageHeight);

        addRenderableWidget(mainPanel);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY)
    {
        renderBackground(context);
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY)
    {
    }

    public static class DelayConfigWidget extends IntListConfigWidget
    {
        public DelayConfigWidget(int x, int y, int w, int h, NumberListConfigHandler<Integer, IntList> handler)
        {
            super(x, y, w, h, handler);
        }

        @Override
        protected void createTextFields()
        {
            addTextField(new TextField(textRenderer, x, y, w, 8, NeepMeat.translationKey("tooltip", "delay.delay")));
        }
    }
}
