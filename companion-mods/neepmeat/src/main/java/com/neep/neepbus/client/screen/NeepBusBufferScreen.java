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
import com.neep.meatlib.client.screen.auto.VStackPanel;
import com.neep.meatlib.client.screen.widget.config.StringConfigField;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepbus.screen.NeepBusBufferScreenHandler;
import com.neep.neepbus.util.message.DataVariant;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class NeepBusBufferScreen extends BetterHandledScreen<NeepBusBufferScreenHandler>
{
    private final VStackPanel mainPanel;
    private final AddressConfigWidget portConfigWidget;
    private final ConfigWidget valueConfig;

    public NeepBusBufferScreen(NeepBusBufferScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);

        this.portConfigWidget = new AddressConfigWidget(handler.configHandler);
        this.valueConfig = new ConfigWidget(0, 0, 100, 100, handler.valueWidgetHandler);

        this.mainPanel = new VStackPanel(1, 12, true)
                .addChild(portConfigWidget)
                .addChild(valueConfig);

        ScreenReady.ready();
    }

    @Override
    protected void init()
    {
        super.init();

        portConfigWidget.init();
        imageWidth = portConfigWidget.nw();
        imageHeight = portConfigWidget.nh() + 20;

        super.init();

        valueConfig.setW(portConfigWidget.nw());

        mainPanel.setDims(leftPos, topPos, imageWidth, imageHeight);

        addRenderableWidget(mainPanel);
    }

    public static class ConfigWidget extends com.neep.meatlib.client.screen.widget.config.ConfigWidget<NeepBusBufferScreenHandler.ValueWidgetHandler.Values>
    {
        private final StringConfigField highValue;

        public ConfigWidget(int x, int y, int w, int h, NeepBusBufferScreenHandler.ValueWidgetHandler handler)
        {
            super(x, y, w, h, handler);

            highValue = addTrackedField(new StringConfigField(textRenderer, x, y, w, 8, NeepMeat.translationKey("screen", "neepbus_buffer.value"), f -> update()));
        }

        @Override
        protected NeepBusBufferScreenHandler.ValueWidgetHandler.Values createValues()
        {
            return new NeepBusBufferScreenHandler.ValueWidgetHandler.Values(DataVariant.of(highValue.parse()));
        }

        @Override
        protected void updateParams(NeepBusBufferScreenHandler.ValueWidgetHandler.Values values)
        {
            highValue.setTextSilent(values.highValue().stringValue());
        }
    }
}
