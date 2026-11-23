/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.plc.robot;

import com.neep.neepmeat.api.plc.PLC;

public class DelayAction implements RobotAction {
    private final int waitTicks;
    private int ticks;

    public DelayAction(int ticks) {
        this.waitTicks = ticks;
    }

    @Override
    public boolean finished(PLC plc) {
        return ticks >= waitTicks;
    }

    @Override
    public void start(PLC plc) {
    }

    @Override
    public void tick(PLC plc) {
        ++ticks;
    }

    @Override
    public void end(PLC plc) {
        ticks = 0;
    }
}
