/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Item and fluid lookups use BlockCapabilityCache. Other lookups call BlockApiLookup.find.
 * Capability insert and extract ignore the Fabric transaction.
 */

package net.fabricmc.fabric.api.lookup.v1.block;

import com.neep.meatlib.transfer.FluidHandlerStorage;
import com.neep.meatlib.transfer.ItemHandlerStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public final class BlockApiCache<A, C> {
    private final BlockApiLookup<A, C> lookup;
    private final ServerLevel level;
    private final BlockPos pos;
    private BlockCapabilityCache<IItemHandler, Direction> items;
    private BlockCapabilityCache<IFluidHandler, Direction> fluids;
    private Direction cachedSide;

    private BlockApiCache(BlockApiLookup<A, C> lookup, ServerLevel level, BlockPos pos) {
        this.lookup = lookup;
        this.level = level;
        this.pos = pos;
    }

    public static <A, C> BlockApiCache<A, C> create(BlockApiLookup<A, C> lookup, ServerLevel level, BlockPos pos) {
        return new BlockApiCache<>(lookup, level, pos);
    }

    @SuppressWarnings("unchecked")
    public A find(C context) {
        if (context instanceof Direction side && lookup.getId() != null && "neepmeat".equals(lookup.getId().getNamespace())) {
            if ("item_storage".equals(lookup.getId().getPath())) {
                if (items == null || cachedSide != side) {
                    items = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, level, pos, side);
                    cachedSide = side;
                }
                IItemHandler handler = items.getCapability();
                return handler == null ? null : (A) ItemHandlerStorage.of(handler);
            }
            if ("fluid_storage".equals(lookup.getId().getPath())) {
                if (fluids == null || cachedSide != side) {
                    fluids = BlockCapabilityCache.create(Capabilities.FluidHandler.BLOCK, level, pos, side);
                    cachedSide = side;
                }
                IFluidHandler handler = fluids.getCapability();
                return handler == null ? null : (A) FluidHandlerStorage.of(handler);
            }
        }
        return lookup.find(level, pos, context);
    }

    public BlockEntity getBlockEntity() {
        return level.getBlockEntity(pos);
    }
}
