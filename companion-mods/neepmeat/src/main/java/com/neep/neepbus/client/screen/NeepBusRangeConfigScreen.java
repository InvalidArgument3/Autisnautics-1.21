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

import com.neep.neepbus.screen.NeepBusRangeConfigScreenHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class NeepBusRangeConfigScreen extends NeepBusConfigScreen<NeepBusRangeConfigScreenHandler>
{
    private final RangeConfigWidget rangeConfig;

    public NeepBusRangeConfigScreen(NeepBusRangeConfigScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);

        rangeConfig = new RangeConfigWidget(6, 0, 100, 100, handler.rangeConfig);
    }

    @Override
    protected void init()
    {
        super.init();

        addRenderableWidget(rangeConfig);

        // Attach the range config to the bottom of the port config
        rangeConfig.setW(imageWidth);
        rangeConfig.init();

        rangeConfig.setPos(leftPos, topPos + imageHeight + 12);
        imageHeight += rangeConfig.nh();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    protected void containerTick()
    {
        super.containerTick();
        rangeConfig.tick();
    }

    @Override
    public void onClose()
    {
        rangeConfig.update();
        super.onClose();
    }
}
