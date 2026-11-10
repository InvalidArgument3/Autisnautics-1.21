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

package com.neep.meatweapons.init;

import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.screen.TinkerTableScreenHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class MWScreenHandlers
{
    public static MenuType<TinkerTableScreenHandler> MEATGUN = new MenuType<>(TinkerTableScreenHandler::new, FeatureFlags.VANILLA_SET);

    public static void init()
    {
        MEATGUN = Registry.register(BuiltInRegistries.MENU, new ResourceLocation(MeatWeapons.NAMESPACE, "meatgun"), MEATGUN);
    }
}
