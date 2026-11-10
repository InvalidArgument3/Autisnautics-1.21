/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.screen;

import com.neep.meatlib.MeatLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Menu registry. No menus are registered in this milestone. Machines call {@link #register}
 * with a {@code MenuType} from {@code IMenuTypeExtension.create}. The register is attached
 * to the mod bus from {@link com.neep.meatlib.MeatLib}.
 */
public final class MeatlibMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MeatLib.ID);

    private MeatlibMenus() {}

    public static <M extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<M>> register(String path, Supplier<MenuType<M>> factory) {
        return MENUS.register(path, factory);
    }
}
