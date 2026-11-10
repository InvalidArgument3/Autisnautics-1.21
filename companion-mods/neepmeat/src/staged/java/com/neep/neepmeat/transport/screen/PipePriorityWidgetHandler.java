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

package com.neep.neepmeat.transport.screen;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.screen.NumberListConfigHandler;
import com.neep.neepmeat.transport.api.item_network.PriorityPipe;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.entity.player.Player;

public class PipePriorityWidgetHandler extends NumberListConfigHandler<Integer, IntList>
{
    private final PriorityPipe priorityPipe;

    protected PipePriorityWidgetHandler(Player player, PriorityPipe priorityPipe)
    {
        super(player, ParamCodec.INT_LIST);
        this.priorityPipe = priorityPipe;
    }

    @Override
    protected void setValues(IntList list)
    {
        priorityPipe.setOutputPriority(list.getInt(0), list.getInt(1));
    }

    @Override
    protected IntList getValues()
    {
        return IntArrayList.of(priorityPipe.getOutputPriority(), priorityPipe.getInputPriority());
    }
}
