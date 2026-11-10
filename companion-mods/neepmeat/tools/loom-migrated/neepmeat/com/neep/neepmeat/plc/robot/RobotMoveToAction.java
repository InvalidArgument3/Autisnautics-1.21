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

package com.neep.neepmeat.plc.robot;

import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.plc.robot.RobotAction;
import net.minecraft.core.BlockPos;
import com.neep.meatlib.neepasm.NeepASM;

public class RobotMoveToAction implements RobotAction
{
    protected final BlockPos target;

    public RobotMoveToAction(BlockPos target)
    {
        this.target = target;
    }

    @Override
    public boolean finished(PLC plc)
    {
        return plc.getActuator().reachedTarget(plc);
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        plc.getActuator().setTarget(plc, target.above());
    }

    @Override
    public void tick(PLC plc)
    {
    }

    @Override
    public void end(PLC plc)
    {

    }
}
