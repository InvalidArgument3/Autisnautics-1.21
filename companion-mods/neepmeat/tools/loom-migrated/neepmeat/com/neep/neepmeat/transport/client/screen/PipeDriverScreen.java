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

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.auto.BlackBackgroundPanel;
import com.neep.meatlib.client.screen.auto.HashedBackgroundPanel;
import com.neep.meatlib.client.screen.auto.LabeledPanel;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.transport.client.screen.widget.RequestListWidget;
import com.neep.neepmeat.transport.screen.PipeDriverScreenHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PipeDriverScreen extends BetterHandledScreen<PipeDriverScreenHandler>
{
    private final RequestListWidget requestListWidget;

    public PipeDriverScreen(PipeDriverScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);

        requestListWidget = new RequestListWidget(handler.requestList);
    }

    @Override
    protected void init()
    {
        imageWidth = 200;
        imageHeight = 200;
        super.init();

        var main = new HashedBackgroundPanel(new BlackBackgroundPanel(new LabeledPanel(() ->
        {
            if (requestListWidget.children().isEmpty())
                return Component.nullToEmpty("No active deferred requests");
            else
                return Component.nullToEmpty("Active deferred requests");
        }, requestListWidget)
                .centre(true)
                .col(PLCCols.TEXT.col)));

        addRenderableWidget(main);
        main.setDims(leftPos, topPos, imageWidth, imageHeight);

        ScreenReady.ready();
    }
}
