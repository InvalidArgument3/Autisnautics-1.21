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

package com.neep.neepmeat.api.live_machine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

@FunctionalInterface
public interface StructurePropertyFormatter
{
    DecimalFormat DEFAULT_FLOAT_FORMAT = new DecimalFormat("####.###");
    DecimalFormat E_FLOAT_FORMAT = new DecimalFormat("#.###E0");

    String format(float value);

    static String formatRepair(float repair)
    {
        float percentRepairPerTick = repair * 20 * 100;
        if (!Float.isFinite(percentRepairPerTick))
            return "?";

        BigDecimal bigDecimal = new BigDecimal(percentRepairPerTick).setScale(3, RoundingMode.HALF_UP);
        return bigDecimal.toEngineeringString() + "/s";
    }
}
