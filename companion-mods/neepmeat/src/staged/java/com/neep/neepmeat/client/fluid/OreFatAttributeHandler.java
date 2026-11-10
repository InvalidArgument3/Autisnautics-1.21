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

package com.neep.neepmeat.client.fluid;

import com.neep.neepmeat.api.processing.OreFatRegistry;
import com.neep.neepmeat.init.NMFluids;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.FluidVariantAttributeHandler;
import net.minecraft.network.chat.Component;

@SuppressWarnings("UnstableApiUsage")
public class OreFatAttributeHandler implements FluidVariantAttributeHandler
{
    public Component getName(FluidVariant fluidVariant)
    {
        OreFatRegistry.Entry entry = OreFatRegistry.getFromVariant(fluidVariant);
        if (entry != null)
        {
            // TODO: use an enum or something
            if (fluidVariant.isOf(NMFluids.STILL_DIRTY_ORE_FAT))
                return entry.dirtyFatName();
            else
                return entry.cleanFatName();
        }
        return fluidVariant.getFluid().defaultFluidState().createLegacyBlock().getBlock().getName();
    }
}
