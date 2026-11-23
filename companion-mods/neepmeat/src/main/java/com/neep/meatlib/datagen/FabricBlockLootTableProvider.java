/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.datagen;

/** Fabric block loot provider. Subclasses implement {@link #generate}. */
public abstract class FabricBlockLootTableProvider {
    public FabricBlockLootTableProvider(FabricDataOutput output) {}

    public void generate() {}

    public void dropSelf(net.minecraft.world.level.block.Block block) {}

    public void dropOther(net.minecraft.world.level.block.Block block, net.minecraft.world.level.ItemLike item) {}

    public void add(net.minecraft.world.level.block.Block block, net.minecraft.world.level.storage.loot.LootTable.Builder builder) {}
}
