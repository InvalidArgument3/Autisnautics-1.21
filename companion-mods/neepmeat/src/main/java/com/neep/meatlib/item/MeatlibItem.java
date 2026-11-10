/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.item;

public interface MeatlibItem {
    default TooltipSupplier meatlibTooltip() {
        return TooltipSupplier.BLANK;
    }
}
