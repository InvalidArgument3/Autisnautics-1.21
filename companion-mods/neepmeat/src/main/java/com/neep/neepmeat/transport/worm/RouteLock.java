/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.transport.worm;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record RouteLock(ResourceKey<Level> worldKey, BlockPos destPos) {
    public RouteLock(Level world, BlockPos pos) {
        this(world.dimension(), pos);
    }

    public static @Nullable RouteLock fromNbt(CompoundTag nbt) {
        if (nbt.isEmpty()) {
            return null;
        }
        return new RouteLock(Level.RESOURCE_KEY_CODEC.decode(NbtOps.INSTANCE, nbt.getCompound("world"))
                .result().orElse(new Pair<>(Level.OVERWORLD, new CompoundTag())).getFirst(), BlockPos.of(nbt.getLong("dest")));
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        Tag worldNbt = Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, worldKey).result().orElse(new CompoundTag());
        nbt.put("world", worldNbt);
        nbt.putLong("dest", destPos.asLong());
        return nbt;
    }

    public void release(MinecraftServer server) {
        Level world = world(server);
        if (world != null) {
            DistributorRouting.get(world).releaseRoute(destPos);
        }
    }

    public @Nullable Level world(MinecraftServer server) {
        return server.getLevel(worldKey);
    }
}
