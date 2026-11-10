/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * The vanilla baked-model path does not call emitBlockQuads. Dynamic models
 * read the quads baked by QuadEmitter instead.
 */

package com.neep.meatlib.client.renderer.model;

import com.neep.meatlib.client.renderer.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;

import java.util.function.Supplier;

public interface FabricBakedModel extends BakedModel, IDynamicBakedModel {
    boolean isVanillaAdapter();

    void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context);

    default void emitItemQuads(net.minecraft.world.item.ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {}
}
