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

package com.neep.neepbus.screen;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.screen.NumberListConfigHandler;
import com.neep.neepbus.block.entity.Delay;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.entity.player.Player;

public class DelayConfigHandler extends NumberListConfigHandler<Integer, IntList>
{
    private final Delay delay;

    protected DelayConfigHandler(Player player, Delay delay)
    {
        super(player, ParamCodec.INT_LIST);
        this.delay = delay;
    }

    @Override
    protected void setValues(IntList list)
    {
        delay.setDelay(list.getInt(0));
    }

    @Override
    protected IntList getValues()
    {
        return IntList.of(delay.getDelay());
    }
}
