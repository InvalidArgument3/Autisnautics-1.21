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

package com.neep.neepmeat.api;

import com.neep.neepmeat.NeepMeat;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class DataType
{
    public static final Registry<DataType> REGISTRY = FabricRegistryBuilder.createSimple(DataType.class,
            ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "data_type")).buildAndRegister();

    public static final DataType BLANK = Registry.register(REGISTRY, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "blank"), new DataType());
    public static final DataType NORMAL = Registry.register(REGISTRY, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "normal"), new DataType());
    public static final DataType DIVINE = Registry.register(REGISTRY, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "divine"), new DataType());

    public static void init()
    {

    }
}
