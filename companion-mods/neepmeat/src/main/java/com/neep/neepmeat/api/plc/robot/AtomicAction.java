/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.plc.robot;

import com.neep.neepmeat.api.plc.PLC;

public interface AtomicAction extends RobotAction {
    static AtomicAction of(PLC.PLCConsumer action) {
        return action::accept;
    }

    @Override
    default boolean finished(PLC plc) {
        return true;
    }

    @Override
    default void tick(PLC plc) {
    }

    @Override
    default void end(PLC plc) {
    }
}
