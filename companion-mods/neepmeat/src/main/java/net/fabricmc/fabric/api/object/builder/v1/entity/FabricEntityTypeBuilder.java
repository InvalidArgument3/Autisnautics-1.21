/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */


package net.fabricmc.fabric.api.object.builder.v1.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class FabricEntityTypeBuilder<T extends Entity> {
    private final EntityType.Builder<T> builder;

    private FabricEntityTypeBuilder(EntityType.Builder<T> builder) { this.builder = builder; }

    public static <T extends Entity> FabricEntityTypeBuilder<T> create(MobCategory category, EntityType.EntityFactory<T> factory) {
        return new FabricEntityTypeBuilder<>(EntityType.Builder.of(factory, category));
    }

    public FabricEntityTypeBuilder<T> dimensions(EntityDimensions dimensions) {
        builder.sized(dimensions.width(), dimensions.height());
        return this;
    }

    public FabricEntityTypeBuilder<T> trackRangeChunks(int chunks) {
        builder.clientTrackingRange(chunks);
        return this;
    }

    public FabricEntityTypeBuilder<T> trackedUpdateRate(int rate) {
        builder.updateInterval(rate);
        return this;
    }

    public FabricEntityTypeBuilder<T> forceTrackedVelocityUpdates(boolean value) {
        builder.setShouldReceiveVelocityUpdates(value);
        return this;
    }

    public EntityType<T> build() {
        return builder.build("");
    }
}
