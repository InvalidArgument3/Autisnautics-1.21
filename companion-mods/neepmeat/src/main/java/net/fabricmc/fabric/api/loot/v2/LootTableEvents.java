/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.loot.v2;

import com.neep.meatlib.event.RegisterEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootTable;

public final class LootTableEvents {
    public static final RegisterEvent<Modify> MODIFY = new RegisterEvent<>();
    private LootTableEvents() {}
    @FunctionalInterface
    public interface Modify {
        void modify(ResourceManager resourceManager, Object lootManager, ResourceLocation id, LootTable.Builder tableBuilder, Object source);
    }
}
