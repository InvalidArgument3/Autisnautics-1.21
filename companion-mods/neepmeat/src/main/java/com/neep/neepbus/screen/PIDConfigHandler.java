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
import com.neep.neepbus.block.entity.PIDController;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.world.entity.player.Player;

public class PIDConfigHandler extends NumberListConfigHandler<Float, FloatList>
{
    private final PIDController pid;

    public PIDConfigHandler(Player player, PIDController pid)
    {
        super(player, ParamCodec.FLOAT_LIST);
        this.pid = pid;
    }

    @Override
    protected void setValues(FloatList list)
    {
        pid.setDt(Math.round(list.getFloat(0)));
        pid.setKP(list.getFloat(1));
        pid.setKI(list.getFloat(2));
        pid.setKD(list.getFloat(3));
        pid.setWindupLimit(list.getFloat(4));
        pid.markBlockDirty();
    }

    @Override
    protected FloatList getValues()
    {
        return FloatList.of(pid.getDt(), pid.getKP(), pid.getKI(), pid.getKD(), pid.getWindupLimit());
    }
}
