/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * NeoForge calls {@link net.neoforged.neoforge.client.model.IDynamicBakedModel#getQuads}.
 * That path runs {@link #emitBlockQuads} and keeps every quad {@code meshConsumer} accepts.
 * Emissive material flags are not applied.
 */

package com.neep.meatlib.client.renderer.model;

import com.neep.meatlib.client.renderer.render.CollectingRenderContext;
import com.neep.meatlib.client.renderer.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public interface FabricBakedModel extends BakedModel, IDynamicBakedModel {
    boolean isVanillaAdapter();

    void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context);

    default void emitItemQuads(net.minecraft.world.item.ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {}

    /**
     * Runs {@link #emitBlockQuads} with no world. Connection-sensitive models must return
     * immediately when the world is null. Static meshes still reach {@code meshConsumer}.
     */
    @Override
    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        CollectingRenderContext context = new CollectingRenderContext();
        emitBlockQuads(null, state, BlockPos.ZERO, () -> rand, context);
        return context.quads(side);
    }
}
