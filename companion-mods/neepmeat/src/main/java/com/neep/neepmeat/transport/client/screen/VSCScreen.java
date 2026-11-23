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

import com.neep.meatlib.client.screen.auto.*;
import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepbus.client.screen.OpenNEEPBusConfigButton;
import com.neep.neepmeat.transport.screen.VSCScreenHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.neep.meatlib.theme.PLCCols.ENTRY_H;

public class VSCScreen extends BetterHandledScreen<VSCScreenHandler>
{
    private final Panel main;

    public VSCScreen(VSCScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);

        this.main = new HashedBackgroundPanel(new BlackBackgroundPanel(
                new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                        .setLeft(new VSCConfigWidget(handler.widgetHandler))
                        .setRight(new SnapPanel<>(SnapPanel.Snap.RIGHT, new OpenNEEPBusConfigButton(handler.openConfigC2S.emitter())))
                        .setDivide(new Divide.Abs(Anchor.END, ENTRY_H))
        ));
    }

    @Override
    protected void init()
    {
        ScreenReady.ready();

        this.imageWidth = 200;
        this.imageHeight = 100;

        super.init();

        main.setDims(leftPos, topPos, imageWidth, imageHeight);
        addRenderableWidget(main);
    }
}
