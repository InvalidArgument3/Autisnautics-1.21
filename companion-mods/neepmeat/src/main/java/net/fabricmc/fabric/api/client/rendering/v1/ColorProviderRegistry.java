/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.client.rendering.v1;

import com.neep.meatlib.event.FabricClientEvents;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public final class ColorProviderRegistry<T, P> {
    public static final ColorProviderRegistry<BlockColor, Block> BLOCK = new ColorProviderRegistry<>(true);
    public static final ColorProviderRegistry<ItemColor, ItemLike> ITEM = new ColorProviderRegistry<>(false);
    private final boolean block;

    private ColorProviderRegistry(boolean block) { this.block = block; }

    @SafeVarargs
    public final void register(T color, P... targets) {
        if (block) {
            FabricClientEvents.blockColor((BlockColor) color, (Block[]) targets);
        } else {
            FabricClientEvents.itemColor((ItemColor) color, (ItemLike[]) targets);
        }
    }
}
