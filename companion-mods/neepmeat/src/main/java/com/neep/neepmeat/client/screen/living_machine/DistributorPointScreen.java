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

package com.neep.neepmeat.client.screen.living_machine;

import com.neep.meatlib.client.screen.auto.*;
import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepbus.client.screen.OpenNEEPBusConfigButton;
import com.neep.neepmeat.client.screen.living_machine.widget.DistributorCooldownWidget;
import com.neep.neepmeat.client.screen.living_machine.widget.DistributorPointWidget;
import com.neep.neepmeat.screen_handler.DistributorPointScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DistributorPointScreen extends BetterHandledScreen<DistributorPointScreenHandler>
{
    private final Panel main;

    public DistributorPointScreen(DistributorPointScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);

        var button = new OpenNEEPBusConfigButton(() -> handler.openConfigC2S.emitter().run());

        this.main = new HashedBackgroundPanel(new BlackBackgroundPanel(
                new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                        .setLeft(new DistributorPointWidget(handler.handler))
                        .setRight(new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
//                                .setLeft(button)
                                .setLeft(new DistributorCooldownWidget(Minecraft.getInstance().font, handler.cooldownHandler))
                                .setRight(button)
                                .setDivide(new Divide.Abs(Anchor.END, 16))
                                .separation()
//                                .setFit(SplitPanel.Fit.SECOND)
                        )
                        .setDivide(new Divide.Abs(Anchor.END, 16))
                ));
    }

    @Override
    protected void init()
    {
        ScreenReady.ready();

//        addressConfigWidget.init();
        imageWidth = 200;
        imageHeight = 166;

        super.init();

        addRenderableWidget(main);

        main.setDims(leftPos, topPos, imageWidth, imageHeight);
        main.init();
    }

    private void thing()
    {
    }
}
