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

import com.neep.neepmeat.machine.reactor.block.entity.CoreSensorBlockEntity;
import net.minecraft.util.Mth;

public class ReactionCoreParameters
{
    private boolean dead;

    private double organisation = 0;
    private double organisationFlow = 0;
    private double incidentZoneRadius = 0;
    private double exudateMassFlow = 0;
    private double storedExudate = 0;
    private double cudEfficiency = 0;

    public void tick(float disruption, float maxOrganisation, float maxOrganisationIncrease, int dt)
    {
        float km = 0.04f / 100; // Organisation to mass flow
        float ke = 1;

        organisationFlow = Math.min(maxOrganisationIncrease * dt, maxOrganisation - organisation);

        organisation = Mth.clamp(organisation + organisationFlow, 0, maxOrganisation);
        // Recalculate this while organisation can saturate
        organisationFlow = Math.min(maxOrganisationIncrease * 5, maxOrganisation - organisation);
        organisation = Mth.clamp(organisation - disruption * dt, 0, maxOrganisation);

        exudateMassFlow = km * organisation;
        storedExudate += exudateMassFlow;

        if (maxOrganisation == 0)
            cudEfficiency = 0;
        else
            cudEfficiency = ke * Mth.clamp(organisationFlow, 0, maxOrganisation) / maxOrganisation;

//        NeepMeat.LOGGER.info("Organisation {}, Cud efficiency {}, Exudate mass flow {}, Stored exudate {}, Incident zone radius {}",
//                organisation, cudEfficiency, exudateMassFlow, storedExudate, incidentZoneRadius);

        if (organisation == 0)
        {
            dead = true;
        }
    }

    public void tickIncidentZone()
    {
        float k1 = 0.03f;
        float k2 = 0.1f;
        incidentZoneRadius = k1 * storedExudate + k2 * exudateMassFlow;
    }

    public void emitSensorData(CoreSensorBlockEntity sensor)
    {
        sensor.organisation.send((float) organisation);
        sensor.incidentZoneRadius.send((float) incidentZoneRadius);
        sensor.exudateFlow.send((float) (exudateMassFlow * 200)); // Units per second
        sensor.storedExudate.send((float) (storedExudate * 10)); // Units (1/10 of a block)
        sensor.cudEfficiency.send((float) (cudEfficiency * 100));
    }

    public double getIncidentZoneRadius()
    {
        return incidentZoneRadius;
    }

    public double getCudEfficiency()
    {
        return cudEfficiency;
    }

    public double getStoredExudate()
    {
        return storedExudate;
    }

    public void extractStored(double toExtract)
    {
        storedExudate -= toExtract;
    }

    public boolean isDead()
    {
        return dead;
    }
}
