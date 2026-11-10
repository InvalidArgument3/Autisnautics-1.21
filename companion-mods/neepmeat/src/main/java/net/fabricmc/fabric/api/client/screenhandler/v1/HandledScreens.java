/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.client.screenhandler.v1;

import com.neep.meatlib.event.FabricClientEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public final class HandledScreens {
    private HandledScreens() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(MenuType type, MenuScreens.ScreenConstructor constructor) {
        FabricClientEvents.screen(type, constructor);
    }
}
