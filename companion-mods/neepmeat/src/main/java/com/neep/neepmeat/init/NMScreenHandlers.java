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

package com.neep.neepmeat.init;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.screen_handler.ItemInputScreenHandler;
import com.neep.neepmeat.screen_handler.ItemOutputScreenHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NMScreenHandlers {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, NeepMeat.ID);
    public static final DeferredHolder<MenuType<?>, MenuType<ItemInputScreenHandler>> ITEM_INPUT = MENUS.register("item_input",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new ItemInputScreenHandler(id, inventory)));
    public static final DeferredHolder<MenuType<?>, MenuType<ItemOutputScreenHandler>> ITEM_OUTPUT = MENUS.register("item_output",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new ItemOutputScreenHandler(id, inventory)));

    private NMScreenHandlers() {}

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType ADVANCED_ROUTER;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType ALLOY_KILN;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType BUFFER_SCREEN_HANDLER;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType BUILDER;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType CONTENT_DETECTOR_SCREEN_HANDLER;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType DEPLOYER;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType DISPLAY_PLATE;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType DISTRIBUTOR_POINT;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType FABRICATOR;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType FILTER;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType FLUID_RATIONER;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType GUIDE;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType IMPLANTER;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType LIVING_MACHINE;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType PHAGE_RAY;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType PLC;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType PROGRAM_CABINET;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType PROGRAM_CARD;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType ROUTER;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType SEPARATOR;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType SMALL_COMPRESSOR;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType STIRLING_ENGINE;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType UPGRADE_MANAGER;
    @SuppressWarnings("rawtypes")
    public static net.minecraft.world.inventory.MenuType VASCULAR_CONDENSER;
}
