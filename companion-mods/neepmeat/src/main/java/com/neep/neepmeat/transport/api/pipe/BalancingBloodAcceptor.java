/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * {@code update} returns the change in absolute units. A negative value removes energy.
 */

package com.neep.neepmeat.transport.api.pipe;

public interface BalancingBloodAcceptor extends BloodAcceptor {
    long update(long internal);

    int priority();

    @Override
    default Mode mode() {
        return Mode.BALANCE;
    }
}
