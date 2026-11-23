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

package com.neep.neepmeat.client.implant;

import com.neep.meatlib.client.screen.auto.SplitPanel;
import com.neep.neepmeat.client.screen.ImplantManagerScreen;
import com.neep.neepmeat.implant.config.ExtraMouthConfigHandler;
import com.neep.neepmeat.screen_handler.ImplantManagerScreenHandler;
import com.neep.neepmeat.transport.client.screen.filter.FilterButtonsPanel;
import com.neep.neepmeat.transport.client.screen.filter.FilterPanel;

public class ExtraMouthConfigPanel extends ImplantConfigPanel<ExtraMouthConfigHandler>
{
    private final ImplantManagerScreen screen;

    public ExtraMouthConfigPanel(ImplantManagerScreenHandler screenHandler, ImplantManagerScreen screen)
    {
        super(screenHandler, ExtraMouthConfigHandler.class);
        this.screen = screen;
    }

    @Override
    protected void onInit()
    {
        FilterPanel filter = new FilterPanel(configHandler.filterHandler, screenHandler::getCarried);

        FilterButtonsPanel buttons = new FilterButtonsPanel(configHandler.filterHandler, 1, 1);

        main = new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                .setLeft(filter)
                .setRight(buttons)
                .setFit(SplitPanel.Fit.SECOND);
    }
}
