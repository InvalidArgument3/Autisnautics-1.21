/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.robot;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.plc.robot.RobotAction;
import net.minecraft.core.BlockPos;

public class RobotMoveToAction implements RobotAction {
    protected final BlockPos target;

    public RobotMoveToAction(BlockPos target) {
        this.target = target;
    }

    @Override
    public boolean finished(PLC plc) {
        return plc.getActuator().reachedTarget(plc);
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException {
        plc.getActuator().setTarget(plc, target.above());
    }

    @Override
    public void tick(PLC plc) {
    }

    @Override
    public void end(PLC plc) {
    }
}
