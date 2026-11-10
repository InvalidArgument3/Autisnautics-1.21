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

package com.neep.meatweapons.meatgun;

import static com.neep.meatweapons.meatgun.AmmunitionProvider.F;

public enum AmmunitionType
{
    ENERGY("ENERG", 1f / F),
    BALLISTIC("BLSTC", 1f / F),
    BLOOD("BLOOD", 0.2f / F),
    METABOLIC("METAB", 0.0f),
    AIR("  AIR", 0.0f),
    ;

    private final String string;
    public final float healthPerUnit;

    AmmunitionType(String string, float healthPerUnit)
    {
        this.string = string;
        this.healthPerUnit = healthPerUnit;
    }

    public String shortName()
    {
        return string;
    }
}
