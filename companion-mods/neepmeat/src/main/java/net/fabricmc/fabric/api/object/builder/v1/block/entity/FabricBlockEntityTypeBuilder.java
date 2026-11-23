/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric's builder is BlockEntityType.Builder.
 */

package net.fabricmc.fabric.api.object.builder.v1.block.entity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class FabricBlockEntityTypeBuilder<T extends BlockEntity> {
    private final BlockEntityType.Builder<T> builder;

    private FabricBlockEntityTypeBuilder(BlockEntityType.Builder<T> builder) {
        this.builder = builder;
    }

    public static <T extends BlockEntity> FabricBlockEntityTypeBuilder<T> create(BlockEntityType.BlockEntitySupplier<? extends T> factory, Block... blocks) {
        return new FabricBlockEntityTypeBuilder<>(BlockEntityType.Builder.of(factory, blocks));
    }

    public BlockEntityType<T> build() {
        return builder.build(null);
    }

    public interface Factory<T extends BlockEntity> extends BlockEntityType.BlockEntitySupplier<T> {}
}
