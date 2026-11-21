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
import com.neep.meatlib.client.screen.widget.IntNumberTextField;
import com.neep.meatlib.client.screen.widget.config.ConfigPanel;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.screen.PowerEmitterScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PowerEmitterScreen extends BetterHandledScreen<PowerEmitterScreenHandler>
{
    private final Panel main;

    public PowerEmitterScreen(PowerEmitterScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        this.main = new HashedBackgroundPanel(new BlackBackgroundPanel(new ConfigWidget1(handler.handler)));
    }

    @Override
    protected void init()
    {
        imageWidth = 200;
        imageHeight = 35;

        super.init();

        main.setDims(leftPos, topPos, imageWidth, imageHeight);
        addRenderableWidget(main);

        ScreenReady.ready();
    }

    private static class ConfigWidget1 extends ConfigPanel<PowerEmitterScreenHandler.PowerEmitterConfigWidgetHandler.Values>
    {
        IntNumberTextField power;

        public ConfigWidget1(ConfigWidgetHandler<PowerEmitterScreenHandler.PowerEmitterConfigWidgetHandler.Values> handler)
        {
            super(handler);
            power = addTrackedField(new IntNumberTextField(Minecraft.getInstance().font, 0, 0, 100, 16,
                    Component.nullToEmpty("Output power"), f -> update(), 0, 2000, 10, 100));
            power.tooltip(NeepMeat.translationKey("screen", "power_emitter.output_power.tooltip"));
        }

        @Override
        protected PowerEmitterScreenHandler.PowerEmitterConfigWidgetHandler.Values createValues()
        {
            return new PowerEmitterScreenHandler.PowerEmitterConfigWidgetHandler.Values(power.parse());
        }

        @Override
        protected void updateParams(PowerEmitterScreenHandler.PowerEmitterConfigWidgetHandler.Values values)
        {
            power.setNumberText(values.powerOutput());
        }
    }
}
