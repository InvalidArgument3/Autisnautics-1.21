/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric block lookup shape. Call sites stay. A real capability is the next step.
 */

package net.fabricmc.fabric.api.lookup.v1.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockApiLookup<A, C> {
    private final ResourceLocation id;

    private BlockApiLookup(ResourceLocation id) {
        this.id = id;
    }

    public static <A, C> BlockApiLookup<A, C> get(ResourceLocation id, Class<A> api, Class<C> context) {
        return new BlockApiLookup<>(id);
    }

    public ResourceLocation getId() {
        return id;
    }

    public A find(Level world, BlockPos pos, C context) {
        return null;
    }

    public A find(Level world, BlockPos pos, BlockState state, BlockEntity blockEntity, C context) {
        return null;
    }

    public void registerSelf(Block... blocks) {}

    public void registerSelf(BlockEntityType<?>... types) {}

    public void registerForBlocks(BlockApiProvider<A, C> provider, Block... blocks) {}

    public void registerForBlockEntity(BlockEntityApiProvider<A, C> provider, BlockEntityType<?> type) {}

    public void registerForBlockEntities(BlockEntityApiProvider<A, C> provider, BlockEntityType<?>... types) {}

    public void registerFallback(BlockApiProvider<A, C> provider) {}

    @FunctionalInterface
    public interface BlockApiProvider<A, C> {
        A find(Level world, BlockPos pos, BlockState state, BlockEntity blockEntity, C context);
    }

    @FunctionalInterface
    public interface BlockEntityApiProvider<A, C> {
        A find(BlockEntity blockEntity, C context);
    }
}
