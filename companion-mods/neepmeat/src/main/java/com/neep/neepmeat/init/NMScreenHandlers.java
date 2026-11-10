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
}
