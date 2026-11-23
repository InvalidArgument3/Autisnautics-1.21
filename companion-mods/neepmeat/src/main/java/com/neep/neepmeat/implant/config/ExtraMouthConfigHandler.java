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

package com.neep.neepmeat.implant.config;

import com.neep.neepmeat.implant.entity.ExtraMouthImplant;
import com.neep.neepmeat.screen_handler.ImplantManagerScreenHandler;
import com.neep.neepmeat.transport.screen.FilterWidgetHandler;
import net.minecraft.world.entity.player.Player;

public class ExtraMouthConfigHandler implements ImplantConfigHandler
{
    public final FilterWidgetHandler filterHandler;

    public ExtraMouthConfigHandler(ImplantManagerScreenHandler screenHandler, ExtraMouthImplant implant, Player player)
    {
        filterHandler = new FilterWidgetHandler(player, implant.getFilters());
    }

    @Override
    public void sendUpdates()
    {
        filterHandler.sendUpdates();
    }

    @Override
    public void close()
    {
        filterHandler.close();
    }
}
