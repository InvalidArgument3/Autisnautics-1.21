/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Derivative NeoForge 1.21.1 port for the Autisnautics pack. Upstream source headers
 * release the work under the GNU General Public License, version 3 or later. The
 * LICENSE file shipped with NeepMeat is the GNU Lesser General Public License,
 * version 3. This port does not relicense the work.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.compat;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.compat.jei.NeepJeiClient;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * JEI category for alloy smelting. Recipe lookup touches the client, so the dedicated
 * server only publishes the plugin id.
 */
@JeiPlugin
public class NeepMeatJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        if (FMLEnvironment.dist.isClient()) {
            NeepJeiClient.registerCategories(registration);
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (FMLEnvironment.dist.isClient()) {
            NeepJeiClient.registerRecipes(registration);
        }
    }
}
