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

import com.neep.meatlib.client.screen.widget.FloatNumberTextField;
import com.neep.meatlib.client.screen.widget.IntNumberTextField;
import com.neep.meatlib.client.screen.widget.config.ConfigWidget;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepbus.screen.IntegratorConfigHandler;
import com.neep.neepbus.screen.IntegratorScreenHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class IntegratorScreen extends NeepBusConfigScreen<IntegratorScreenHandler>
{
    private final ConfigWidget1 valuesWidget;

    public IntegratorScreen(IntegratorScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        valuesWidget = new ConfigWidget1(0, 0, 100, 100, handler.integratorHandler);
        ScreenReady.ready();
    }

    @Override
    protected void init()
    {
        super.init();

        addRenderableWidget(valuesWidget);

        // Attach the range config to the bottom of the port config
        valuesWidget.setW(imageWidth);
        valuesWidget.init();

        valuesWidget.setPos(leftPos, topPos + imageHeight + 12);
        imageHeight += valuesWidget.nh();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    private static class ConfigWidget1 extends ConfigWidget<IntegratorConfigHandler.Values>
    {
        private final IntNumberTextField interval = addTrackedField(new IntNumberTextField(textRenderer, x, y, w, 8, Component.nullToEmpty("Interval (ticks): "), c -> update(), 1, Integer.MAX_VALUE));
        private final FloatNumberTextField timeFactor = addTrackedField(new FloatNumberTextField(textRenderer, x, y, w, 8, Component.nullToEmpty("Input gain: "), c -> update(), -Float.MAX_VALUE, Float.MAX_VALUE));

        public ConfigWidget1(int x, int y, int w, int h, ConfigWidgetHandler<IntegratorConfigHandler.Values> handler)
        {
            super(x, y, w, h, handler);
        }

        @Override
        protected IntegratorConfigHandler.Values createValues()
        {
            return new IntegratorConfigHandler.Values(interval.parse(), timeFactor.parse());
        }

        @Override
        protected void updateParams(IntegratorConfigHandler.Values values)
        {
            interval.setNumberText(values.loopTime());
            timeFactor.setNumberText(values.timeFactor());
        }
    }
}
