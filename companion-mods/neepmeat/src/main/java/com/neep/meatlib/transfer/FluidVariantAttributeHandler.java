/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.transfer;

import net.minecraft.network.chat.Component;

public interface FluidVariantAttributeHandler {
    default Component getName(FluidVariant variant) {
        return FluidVariantAttributes.getName(variant);
    }

    default int getColor(FluidVariant variant) {
        return FluidVariantAttributes.getColor(variant);
    }

}
