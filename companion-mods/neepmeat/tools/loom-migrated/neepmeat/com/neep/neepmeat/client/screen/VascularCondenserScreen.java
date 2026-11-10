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

package com.neep.neepmeat.client.screen;

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.auto.HashedBackgroundPanel;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepbus.client.screen.OpenNEEPBusConfigButton;
import com.neep.neepmeat.client.screen.widget.VascularCondenserPanel;
import com.neep.neepmeat.screen_handler.VascularCondenserScreenHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class VascularCondenserScreen extends BetterHandledScreen<VascularCondenserScreenHandler>
{
    private final VascularCondenserPanel panel;

    public VascularCondenserScreen(VascularCondenserScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        panel = new VascularCondenserPanel(handler.widgetHandler);

//        ScreenReady.ready();
    }

    @Override
    protected void init()
    {
        this.imageWidth = 240;
        this.imageHeight = 141;

        super.init();

        var main = new HashedBackgroundPanel(panel);

        main.setDims(leftPos, topPos, imageWidth, imageHeight);

        addRenderableWidget(main);

        ScreenReady.ready();
    }
}
