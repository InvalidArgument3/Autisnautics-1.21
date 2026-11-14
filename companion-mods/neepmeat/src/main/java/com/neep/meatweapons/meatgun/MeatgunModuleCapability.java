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

import java.util.EnumSet;
import java.util.Set;

/// Indicates which vanilla inputs are redirected by a module.
/// Allows a module to enable normal mining when placed in a specific, known slot.
public enum MeatgunModuleCapability
{
    ATTACK, // Enable vanilla attacking
    USE // Enable vanilla using

    ;

    public static final Set<MeatgunModuleCapability> REDIRECT_ALL = EnumSet.allOf(MeatgunModuleCapability.class);
    public static final Set<MeatgunModuleCapability> NONE = EnumSet.noneOf(MeatgunModuleCapability.class);
}
