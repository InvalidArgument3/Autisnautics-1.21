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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public interface ComponentType<T extends LivingMachineComponent>
{
    @SuppressWarnings("unchecked")
    MappedRegistry<ComponentType<?>> REGISTRY = FabricRegistryBuilder.createSimple(
            (Class<ComponentType<?>>) (Object) ComponentType.class, new ResourceLocation(NeepMeat.NAMESPACE, "lm_component_type")).buildAndRegister();

    List<ComponentType<?>> ID_TO_TYPE = new ObjectArrayList<>();

    int getBitIdx();

    class Simple<T extends LivingMachineComponent> implements ComponentType<T>
    {
        static int NEXT_ID = 0;

        private final int id;

        public static int size()
        {
            return NEXT_ID;
        }

        public Simple()
        {
            id = NEXT_ID++;
        }

        public int getBitIdx()
        {
            return id;
        }
    }
}
