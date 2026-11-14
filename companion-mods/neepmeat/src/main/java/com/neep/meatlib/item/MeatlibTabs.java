/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Meatlib's own creative tab. Entries collected by MeatlibItemGroups whose id
 * is in the meatlib namespace are shown here.
 */

package com.neep.meatlib.item;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.util.MeatlibItemGroups;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MeatlibTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MeatLib.ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.meatlib.main"))
            .icon(() -> Items.BOOK.getDefaultInstance())
            .displayItems((params, output) -> MeatlibItemGroups.fillNamespace(MeatLib.ID, output))
            .build());

    private MeatlibTabs() {}

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
