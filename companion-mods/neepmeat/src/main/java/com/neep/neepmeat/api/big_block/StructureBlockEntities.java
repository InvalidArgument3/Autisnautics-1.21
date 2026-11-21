/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Structure block entities are created while the block register event is running,
 * so they are queued and registered on the block-entity event.
 */

package com.neep.neepmeat.api.big_block;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class StructureBlockEntities {
    private static final List<Pending> PENDING = new ArrayList<>();

    private StructureBlockEntities() {}

    public static <T extends BigBlockStructureEntity> BlockEntityType<T> register(String path, BlockEntityType<T> type) {
        PENDING.add(new Pending(ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, path), () -> type));
        return type;
    }

    public static void capture(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
            return;
        }
        for (Pending pending : PENDING) {
            event.register(Registries.BLOCK_ENTITY_TYPE, pending.id, pending.factory);
        }
        PENDING.clear();
    }

    private record Pending(ResourceLocation id, Supplier<BlockEntityType<?>> factory) {}
}
