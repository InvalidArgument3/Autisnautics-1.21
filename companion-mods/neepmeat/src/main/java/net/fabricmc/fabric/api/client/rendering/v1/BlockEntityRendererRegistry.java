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
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BlockEntityRendererRegistry {
    private BlockEntityRendererRegistry() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(BlockEntityType type, BlockEntityRendererProvider provider) {
        FabricClientEvents.blockEntityRenderer(type, provider);
    }
}
