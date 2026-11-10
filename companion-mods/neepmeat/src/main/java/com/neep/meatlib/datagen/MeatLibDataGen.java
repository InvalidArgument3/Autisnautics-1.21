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

package com.neep.meatlib.datagen;

import com.google.common.collect.Sets;
import com.neep.meatlib.datagen.DataGeneratorEntrypoint;
import com.neep.meatlib.datagen.FabricDataGenerator;
import net.minecraft.data.DataProvider;

import java.util.Set;

public abstract class MeatLibDataGen implements DataGeneratorEntrypoint
{
    private final Set<FabricDataGenerator.Pack.Factory<?>> factories = Sets.newHashSet();
    private final Set<FabricDataGenerator.Pack.RegistryDependentFactory<?>> registryDependent = Sets.newHashSet();

    protected void applyFactories(FabricDataGenerator.Pack pack)
    {
        factories.forEach(pack::addProvider);
        registryDependent.forEach(pack::addProvider);
    }
}
