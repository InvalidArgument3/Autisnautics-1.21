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

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class BlockApiLookup<A, C> {
    private final ResourceLocation id;
    private final Class<A> api;
    private final List<BlockApiProvider<A, C>> fallbacks = new ArrayList<>();
    private final Map<Block, BlockApiProvider<A, C>> blocks = new IdentityHashMap<>();
    private final Map<BlockEntityType<?>, BlockEntityApiProvider<A, C>> blockEntities = new IdentityHashMap<>();

    private BlockApiLookup(ResourceLocation id, Class<A> api) {
        this.id = id;
        this.api = api;
    }

    public static <A, C> BlockApiLookup<A, C> get(ResourceLocation id, Class<A> api, Class<C> context) {
        return new BlockApiLookup<>(id, api);
    }

    public ResourceLocation getId() {
        return id;
    }

    public A find(Level world, BlockPos pos, C context) {
        if (world == null || pos == null) {
            return null;
        }
        return find(world, pos, world.getBlockState(pos), world.getBlockEntity(pos), context);
    }

    public A find(Level world, BlockPos pos, BlockState state, BlockEntity blockEntity, C context) {
        if (blockEntity != null) {
            BlockEntityApiProvider<A, C> provider = blockEntities.get(blockEntity.getType());
            if (provider != null) {
                A found = provider.find(blockEntity, context);
                if (found != null) {
                    return found;
                }
            }
        }
        if (state != null) {
            BlockApiProvider<A, C> provider = blocks.get(state.getBlock());
            if (provider != null) {
                A found = provider.find(world, pos, state, blockEntity, context);
                if (found != null) {
                    return found;
                }
            }
        }
        for (BlockApiProvider<A, C> fallback : fallbacks) {
            A found = fallback.find(world, pos, state, blockEntity, context);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public void registerSelf(Block... blocks) {
        registerForBlocks((world, pos, state, blockEntity, context) -> api.isInstance(state.getBlock()) ? api.cast(state.getBlock()) : null, blocks);
    }

    public void registerSelf(BlockEntityType<?>... types) {
        for (BlockEntityType<?> type : types) {
            registerForBlockEntity((blockEntity, context) -> api.isInstance(blockEntity) ? api.cast(blockEntity) : null, type);
        }
    }

    public void registerForBlocks(BlockApiProvider<A, C> provider, Block... blocks) {
        for (Block block : blocks) {
            this.blocks.put(block, provider);
        }
    }

    public void registerForBlockEntity(BlockEntityApiProvider<A, C> provider, BlockEntityType<?> type) {
        blockEntities.put(type, provider);
    }

    public void registerForBlockEntities(BlockEntityApiProvider<A, C> provider, BlockEntityType<?>... types) {
        for (BlockEntityType<?> type : types) {
            registerForBlockEntity(provider, type);
        }
    }

    public void registerFallback(BlockApiProvider<A, C> provider) {
        fallbacks.add(provider);
    }

    @FunctionalInterface
    public interface BlockApiProvider<A, C> {
        A find(Level world, BlockPos pos, BlockState state, BlockEntity blockEntity, C context);
    }

    @FunctionalInterface
    public interface BlockEntityApiProvider<A, C> {
        A find(BlockEntity blockEntity, C context);
    }
}
