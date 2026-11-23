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

import com.neep.meatweapons.MeatWeapons;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

public enum SlotType
{
    NORMAL(true, "normal"),
    BARREL(false, "barrel"),
    DRILL(true, "drill"),
    SIGHT(false, "sight"),
    STOCK(false, "stock"),
    FIRING_MECHANISM(false, "firing_mechanism")
    ;

    public static final Collection<SlotType> ALL_SLOTS = EnumSet.copyOf(Arrays.stream(values()).filter(v -> v.applicableByDefault).toList());

    // If false, ALL_VALUES will not contain it.
    // This means that a module type needs to specifically allow this value to be applicable.
    public final boolean applicableByDefault;
    private final String translationKey;

    SlotType(boolean applicableByDefault, String suffix)
    {
        this.applicableByDefault = applicableByDefault;
        this.translationKey = "slot_type." + MeatWeapons.NAMESPACE + "." + suffix;
    }

    public String translationKey()
    {
        return translationKey;
    }
}
