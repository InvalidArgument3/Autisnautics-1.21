/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Players near a position, or tracking an entity, from the server player list.
 */

package net.fabricmc.fabric.api.networking.v1;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;

public final class PlayerLookup {
    private PlayerLookup() {}

    public static Collection<ServerPlayer> around(Level world, BlockPos pos, double radius) {
        return around(world, Vec3.atCenterOf(pos), radius);
    }

    public static Collection<ServerPlayer> around(Level world, Vec3 pos, double radius) {
        if (!(world instanceof ServerLevel server)) {
            return List.of();
        }
        double radiusSq = radius * radius;
        return server.players().stream().filter(player -> player.position().distanceToSqr(pos) <= radiusSq).toList();
    }

    public static Collection<ServerPlayer> tracking(Entity entity) {
        if (!(entity.level() instanceof ServerLevel server)) {
            return List.of();
        }
        return around(server, entity.blockPosition(), 64);
    }

    public static Collection<ServerPlayer> tracking(BlockEntity blockEntity) {
        return around(blockEntity.getLevel(), blockEntity.getBlockPos(), 64);
    }
}
