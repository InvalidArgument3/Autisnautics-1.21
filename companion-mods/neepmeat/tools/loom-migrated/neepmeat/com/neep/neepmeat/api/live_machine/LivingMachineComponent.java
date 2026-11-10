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

import com.neep.neepmeat.NeepMeat;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.framework.qual.NoDefaultQualifierForUse;
import org.jetbrains.annotations.Nullable;

public interface LivingMachineComponent
{
    BlockApiLookup<LivingMachineComponent, Void> LOOKUP = BlockApiLookup.get(new ResourceLocation(NeepMeat.NAMESPACE, "living_machine_component"),
            LivingMachineComponent.class, Void.class);

    default void setController(@Nullable BlockPos pos)
    {

    }

    boolean componentRemoved();

    ComponentType<? extends LivingMachineComponent> getComponentType();

    default void shutdown()
    {
        setController(null);
    }
}
