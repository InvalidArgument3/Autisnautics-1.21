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

package com.neep.meatlib.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ScreenHandlerRegistry
{
    public static <T extends AbstractContainerMenu> MenuType<T> register(String namespace, String id, MenuType.MenuSupplier<T> factory)
    {
        return Registry.register(BuiltInRegistries.MENU, new ResourceLocation(namespace, id), new MenuType<>(factory, FeatureFlags.VANILLA_SET));
    }

    public static <T extends AbstractContainerMenu> ExtendedScreenHandlerType<T> registerExtended(String namespace, String id, ExtendedScreenHandlerType.ExtendedFactory<T> factory)
    {
        return Registry.register(BuiltInRegistries.MENU, new ResourceLocation(namespace, id), new ExtendedScreenHandlerType<>(factory));
    }
}
