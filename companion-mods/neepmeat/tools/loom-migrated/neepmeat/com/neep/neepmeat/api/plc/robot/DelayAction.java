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

package com.neep.neepmeat.api.plc.robot;

import com.neep.neepmeat.api.plc.PLC;

public class DelayAction implements RobotAction
{
    private final int waitTicks;
    private int ticks;

    public DelayAction(int ticks)
    {
        this.waitTicks = ticks;
    }

    @Override
    public boolean finished(PLC plc)
    {
        return ticks >= waitTicks;
    }

    @Override
    public void start(PLC plc)
    {
    }

    @Override
    public void tick(PLC plc)
    {
        ++ticks;
    }

    @Override
    public void end(PLC plc)
    {
        ticks = 0;
    }
}
