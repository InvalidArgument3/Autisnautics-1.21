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

import com.neep.meatlib.client.screen.widget.TooltipRenderer;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.screen.plc.PLCScreenButton;
import net.minecraft.network.chat.Component;

public class OpenNEEPBusConfigButton extends PLCScreenButton implements TooltipRenderer
{
    private final Runnable openConfig;

    public OpenNEEPBusConfigButton(Runnable openConfig)
    {
        super(0, 0, Component.empty());
        this.openConfig = openConfig;
        tooltip(NeepMeat.translationKey("screen", "distributor_point.neepbus"));
    }

    @Override
    protected int getU()
    {
        return 224;
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        super.onClick(mouseX, mouseY);

        openConfig.run();
    }
};
