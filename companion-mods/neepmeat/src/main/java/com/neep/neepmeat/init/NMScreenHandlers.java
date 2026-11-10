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

    public static final DeferredHolder<MenuType<?>, MenuType<com.neep.neepmeat.screen_handler.BufferScreenHandler>> BUFFER_TYPE = MENUS.register("buffer_screen",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new com.neep.neepmeat.screen_handler.BufferScreenHandler(id, inventory)));
    public static final DeferredHolder<MenuType<?>, MenuType<com.neep.neepmeat.screen_handler.GuideScreenHandler>> GUIDE_TYPE = MENUS.register("guide",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new com.neep.neepmeat.screen_handler.GuideScreenHandler(id, inventory)));
    public static final DeferredHolder<MenuType<?>, MenuType<com.neep.neepmeat.screen_handler.RouterScreenHandler>> ROUTER_TYPE = MENUS.register("router",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new com.neep.neepmeat.screen_handler.RouterScreenHandler(id, inventory)));
    public static final DeferredHolder<MenuType<?>, MenuType<com.neep.neepmeat.machine.small_compressor.SmallCompressorScreenHandler>> SMALL_COMPRESSOR_TYPE = MENUS.register("small_compressor",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new com.neep.neepmeat.machine.small_compressor.SmallCompressorScreenHandler(id, inventory)));
    public static final DeferredHolder<MenuType<?>, MenuType<com.neep.neepmeat.screen_handler.StirlingEngineScreenHandler>> STIRLING_ENGINE_TYPE = MENUS.register("stirling_engine",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new com.neep.neepmeat.screen_handler.StirlingEngineScreenHandler(id, inventory)));

    public static void register(IEventBus bus) {
        MENUS.register(bus);
        bus.addListener(NMScreenHandlers::bind);
    }

    private static void bind(net.neoforged.neoforge.registries.RegisterEvent event) {
        if (event.getRegistryKey() != Registries.MENU) {
            return;
        }
        ALLOY_KILN = com.neep.neepmeat.machine.NeepMachines.ALLOY_MENU.get();
        BUFFER_SCREEN_HANDLER = BUFFER_TYPE.get();
        GUIDE = GUIDE_TYPE.get();
        ROUTER = ROUTER_TYPE.get();
        SMALL_COMPRESSOR = SMALL_COMPRESSOR_TYPE.get();
        STIRLING_ENGINE = STIRLING_ENGINE_TYPE.get();
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
