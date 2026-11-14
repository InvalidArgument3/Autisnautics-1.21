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

package com.neep.neepmeat.machine.reactor;

import net.minecraft.util.Mth;

/** Upstream organisation step. Disruption subtracts. That is the instability the disruptor applies. */
public class ReactionCoreParameters {
    private double organisation;
    private float maxOrganisation = 100f;

    public void setOrganisation(double organisation, float maxOrganisation) {
        this.maxOrganisation = maxOrganisation;
        this.organisation = Mth.clamp(organisation, 0, maxOrganisation);
    }

    public double organisation() {
        return organisation;
    }

    public void tick(float disruption, float maxOrganisationIncrease, int dt) {
        double flow = Math.min(maxOrganisationIncrease * dt, maxOrganisation - organisation);
        organisation = Mth.clamp(organisation + flow, 0, maxOrganisation);
        organisation = Mth.clamp(organisation - disruption * dt, 0, maxOrganisation);
    }
}
