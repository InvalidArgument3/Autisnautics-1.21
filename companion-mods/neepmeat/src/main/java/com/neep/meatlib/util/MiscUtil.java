/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.List;

public final class MiscUtil {
    /**
     * Fabric transfer used 81 droplets per milli-bucket. NeoForge fluid stacks are already milli-buckets
     * ({@link FluidType#BUCKET_VOLUME} is 1000). Callers ported from Fabric should divide by 81.
     * Callers that already speak milli-buckets should not use this.
     */
    public static final long FABRIC_DROPLETS_PER_MB = 81L;

    private MiscUtil() {}

    public static long dropletsToMb(long droplets) {
        return Math.floorDiv(droplets, FABRIC_DROPLETS_PER_MB);
    }

    public static boolean canPlayerModify(Player player, BlockPos pos) {
        return player.mayBuild() && player.mayUseItemAt(pos, player.getDirection(), player.getMainHandItem());
    }

    public static <T extends Entity> T closestEntity(List<T> entities, Vec3 pos) {
        T best = null;
        double bestDistance = Double.MAX_VALUE;
        for (T entity : entities) {
            double distance = pos.distanceToSqr(entity.position());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }
        return best;
    }

    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> ticker(BlockEntityType<A> type, BlockEntityTicker<? super E> ticker) {
        return (level, pos, state, blockEntity) -> {
            if (blockEntity.getType() == type) {
                @SuppressWarnings("unchecked")
                E cast = (E) blockEntity;
                ticker.tick(level, pos, state, cast);
            }
        };
    }

    public static <T extends Enum<T>> T cycle(T value, T[] values) {
        return values[(value.ordinal() + 1) % values.length];
    }

    public static void dropBlockEntity(Level level, BlockPos pos, BlockEntity blockEntity) {
        if (level.isClientSide || blockEntity == null) {
            return;
        }
        net.minecraft.world.level.block.Block.dropResources(blockEntity.getBlockState(), level, pos, blockEntity);
    }
}
