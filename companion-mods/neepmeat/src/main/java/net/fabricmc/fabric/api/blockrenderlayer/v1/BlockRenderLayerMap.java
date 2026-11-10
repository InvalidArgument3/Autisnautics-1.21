/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric's render-layer map, written through NeoForge's ItemBlockRenderTypes.
 */

package net.fabricmc.fabric.api.blockrenderlayer.v1;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class BlockRenderLayerMap {
    public static final BlockRenderLayerMap INSTANCE = new BlockRenderLayerMap();

    private BlockRenderLayerMap() {}

    public void putBlock(Block block, RenderType type) {
        ItemBlockRenderTypes.setRenderLayer(block, type);
    }

    public void putBlocks(RenderType type, Block... blocks) {
        for (Block block : blocks) {
            ItemBlockRenderTypes.setRenderLayer(block, type);
        }
    }

    public void putFluid(Fluid fluid, RenderType type) {
        ItemBlockRenderTypes.setRenderLayer(fluid, type);
    }

    public void putFluids(RenderType type, Fluid... fluids) {
        for (Fluid fluid : fluids) {
            if (fluid != null) {
                ItemBlockRenderTypes.setRenderLayer(fluid, type);
            }
        }
    }
}
